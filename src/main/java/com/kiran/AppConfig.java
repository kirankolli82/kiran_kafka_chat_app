package com.kiran;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
@Configuration
public class AppConfig {

    @Bean
    public String userName() {
        return "Kiran";
    }

    @Bean
    public String fxmlLocation() {
        return "chatClient.fxml";
    }
}
