package com.kiran;

import com.kiran.kafka.transport.KafkaTransportLaneFactory;
import com.kiran.zookeeper.ZookeeperContactsTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
@Configuration
@ComponentScan(basePackages = "com.kiran.kafka.transport")
public class AppConfig {

    @Bean
    public TransportLaneFactory transportLaneFactory() {
        return new KafkaTransportLaneFactory("chatTopic", "localhost:9092");
    }

    @Bean
    public ContactsTopic contactsTopic() {
        return new ZookeeperContactsTopic("/chatUsers", "localhost:2181");
    }
}
