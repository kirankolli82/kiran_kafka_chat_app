package com.kiran.gui;

import com.kiran.AppConfig;
import com.kiran.TransportLaneFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public class ChatClient extends Application {
    static Stage primaryStage;
    private static Logger log = LoggerFactory.getLogger(ChatClient.class);
    private TransportLaneFactory transportLaneFactory;


    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatClient.primaryStage = primaryStage;
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        this.transportLaneFactory = applicationContext.getBean("transportLaneFactory", TransportLaneFactory.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("login.fxml"));
        fxmlLoader.setControllerFactory(param -> {
            Optional<Constructor<?>> constructor = Arrays.stream(param.getConstructors())
                    .filter(constructor1 -> (constructor1.getParameterCount() == 1) &&
                            (TransportLaneFactory.class.isAssignableFrom(constructor1.getParameterTypes()[0]))).findFirst();
            if (constructor.isPresent()) {
                try {
                    return constructor.get().newInstance(transportLaneFactory);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Unable to create controller", e);
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        });
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 500, 150);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
