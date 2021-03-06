/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.kiran.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger log = LoggerFactory.getLogger(App.class);

    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        log.info(new App().getGreeting());
    }
}
