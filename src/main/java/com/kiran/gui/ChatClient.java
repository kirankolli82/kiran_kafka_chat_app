package com.kiran.gui;

import com.kiran.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public class ChatClient extends Application {

    private String userName;
    private String fxmlLocation;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        this.userName = applicationContext.getBean("userName", String.class);
        this.fxmlLocation = applicationContext.getBean("fxmlLocation", String.class);
        primaryStage.setTitle(userName);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource(fxmlLocation));

        Scene scene = new Scene(root, 500, 800);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
