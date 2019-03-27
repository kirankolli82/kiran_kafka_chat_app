package com.kiran.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Kiran Kolli on 27-03-2019.
 */
public class ConsumerDemo {

    private static final String GROUP_ID = "my-second-app";
    private static Logger log = LoggerFactory.getLogger(ConsumerDemo.class);

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConsumerRunnable runnable = new ConsumerRunnable(ProducerDemo.BOOTSTRAP_SERVERS, GROUP_ID, latch);
        Thread aThread = new Thread(runnable);
        aThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            runnable.shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        latch.await();
    }

    static class ConsumerRunnable implements Runnable {

        private final CountDownLatch latch;
        private final Properties properties;
        private KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(String bootstrapServer, String groupId, CountDownLatch latch) {
            this.latch = latch;
            this.properties = new Properties();
            this.properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            this.properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            this.properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            this.properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            this.properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            this.consumer = new KafkaConsumer<>(properties);
        }

        @Override
        public void run() {
            try {
                this.consumer.subscribe(Collections.singleton("first_topic"));
                while (true) {
                    ConsumerRecords<String, String> records =
                            this.consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
                    records.forEach(record -> {
                        log.info("Topic: {}", record.topic());
                        log.info("Partition: {}", record.partition());
                        log.info("Offset: {}", record.offset());
                        log.info("Value: {}", record.value());
                    });
                }
            } catch (WakeupException e) {
                log.info("Consumer Runnable woken up, will exit");
            } finally {
                this.consumer.close();
                this.latch.countDown();
            }
        }

        public void shutdown() {
            this.consumer.wakeup();
        }
    }
}
