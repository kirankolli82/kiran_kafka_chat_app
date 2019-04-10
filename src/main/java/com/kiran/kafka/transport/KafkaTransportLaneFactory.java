package com.kiran.kafka.transport;

import com.kiran.ContactsTopic.Contact;
import com.kiran.TransportLaneFactory;
import com.kiran.util.DaemonThreadFactory;
import com.kiran.util.Tuple;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Kiran Kolli on 03-04-2019.
 */

public class KafkaTransportLaneFactory implements TransportLaneFactory {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransportLaneFactory.class);

    private final String topic;
    private final String bootstrapServer;

    private final KafkaProducer<Tuple<Contact, Contact>, Message> producer;

    private final Map<String, KafkaTransportLane> transportLanesByUserId = new HashMap<>();
    private final ExecutorService consumerService = Executors.newCachedThreadPool(new DaemonThreadFactory());
    private final ExecutorService producerService = Executors.newSingleThreadExecutor(new DaemonThreadFactory());

    @Override
    public TransportLane createLaneForUser(String userId) {
        log.info("Creating Kafka Transport Lane for {}", userId);
        Contact currentUser = new Contact(userId);
        ConsumerRunnable consumerRunnable = new ConsumerRunnable(userId, bootstrapServer);
        return transportLanesByUserId.computeIfAbsent(userId, key -> new KafkaTransportLane(currentUser, consumerRunnable));
    }

    public KafkaTransportLaneFactory(String topic, String bootstrapServer) {
        this.topic = topic;
        this.bootstrapServer = bootstrapServer;
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, MessageKeySerializationUtil.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MessageSerializationUtil.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        this.producer = new KafkaProducer<>(properties);
    }


    @SuppressWarnings("unused")
    @PreDestroy
    public void close() {
        log.info("Closing the KafkaTransportLanes");
        this.transportLanesByUserId.values().forEach(KafkaTransportLane::close);
        this.consumerService.shutdown();
        this.producerService.shutdown();
        try {
            this.consumerService.awaitTermination(5, TimeUnit.SECONDS);
            this.producerService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Unable to close all Transport Lanes during exit");
        }
    }

    class KafkaTransportLane implements TransportLane {
        private final Contact currentUser;
        private final ConsumerRunnable consumerRunnable;

        KafkaTransportLane(Contact currentUser, ConsumerRunnable consumerRunnable) {
            log.info("Creating kafka transport lane for : {}", currentUser);
            this.currentUser = currentUser;
            this.consumerRunnable = consumerRunnable;
        }

        @Override
        public Contact getId() {
            return currentUser;
        }

        @Override
        public void subscribeToMessages(Subscriber subscriber) {
            log.info("Subscriber set for {}", currentUser);
            consumerRunnable.subscriberRef.set(subscriber);
            consumerService.submit(consumerRunnable);
        }

        @Override
        public CompletableFuture<Void> sendOnLane(Contact to, String message) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            producerService.submit(() -> {
                ProducerRecord<Tuple<Contact, Contact>, Message> producerRecord = new ProducerRecord<>(topic, new Tuple<>(to, currentUser), new Message(currentUser, message));
                producer.send(producerRecord, (metadata, exception) -> {
                    if (exception != null) {
                        future.completeExceptionally(exception);
                    } else {
                        log.info("Published Message: {}, to topic: {}, partition: {}, at offset: {}", message,
                                metadata.topic(), metadata.partition(), metadata.offset());
                        future.complete(null);
                    }
                });
            });
            return future;
        }

        void close() {
            consumerRunnable.shutdown();
        }
    }

    class ConsumerRunnable implements Runnable {


        private final String userId;
        private final KafkaConsumer<Tuple<Contact, Contact>, Message> consumer;
        private final AtomicReference<Subscriber> subscriberRef = new AtomicReference<>();

        ConsumerRunnable(String userId, String bootstrapServer) {
            this.userId = userId;
            Properties properties = new Properties();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, MessageKeySerializationUtil.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageSerializationUtil.class.getName());
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, userId);
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            this.consumer = new KafkaConsumer<>(properties);
        }

        @Override
        public void run() {
            try {
                this.consumer.subscribe(Collections.singleton(topic));
                //noinspection InfiniteLoopStatement --> will exit when shutdown is invoked which will throw a WakeupException
                while (true) {
                    ConsumerRecords<Tuple<Contact, Contact>, Message> records =
                            this.consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
                    records.forEach(record -> {
                        log.info("User id: {} has received message: {}; from: {}", userId, record.value(), record.key());
                        boolean isOfCurrentUser = (Objects.equals(userId, record.key().getKey().getUserId()))
                                || (Objects.equals(userId, record.key().getValue().getUserId()));
                        if (isOfCurrentUser && subscriberRef.get() != null) {
                            subscriberRef.get().processMessage(record.value().getFrom(), record.value().getMessage());
                        }
                    });
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                }
            } catch (WakeupException e) {
                log.info("Consumer Runnable woken up, will exit");
            } catch (InterruptedException e) {
                log.error("Consumer for {} interrupted during sleep", userId, e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("Consumer for {} has error", userId, e);
            } finally {
                this.consumer.close();
            }
        }

        void shutdown() {
            this.consumer.wakeup();
        }
    }

}
