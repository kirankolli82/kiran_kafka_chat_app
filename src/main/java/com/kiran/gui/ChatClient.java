package com.kiran.gui;

import com.kiran.AppConfig;
import com.kiran.ContactsTopic;
import com.kiran.TransportLaneFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public class ChatClient extends Application {
    static Stage primaryStage;
    private static Logger log = LoggerFactory.getLogger(ChatClient.class);

    private ContactsTopic topic;
    private TransportLaneFactory transportLaneFactory;
    private AnnotationConfigApplicationContext applicationContext;


    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatClient.primaryStage = primaryStage;
        this.applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        this.transportLaneFactory = applicationContext.getBean("transportLaneFactory", TransportLaneFactory.class);
        this.topic = applicationContext.getBean("contactsTopic", ContactsTopic.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("login.fxml"));
        fxmlLoader.setControllerFactory(param -> new LoginController(topic, transportLaneFactory));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 500, 150);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("About to close application");
        this.applicationContext.close();
        log.info("Resources cleaned up");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
