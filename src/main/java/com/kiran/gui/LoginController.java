package com.kiran.gui;

import com.kiran.TransportLane;
import com.kiran.TransportLaneFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Kiran Kolli on 29-03-2019.
 */
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @FXML
    private Button submit;
    @FXML
    private TextField userName;

    private final TransportLaneFactory transportLaneFactory;

    public LoginController(TransportLaneFactory transportLaneFactory) {
        this.transportLaneFactory = transportLaneFactory;
    }


    @FXML
    public void initialize() {
        userName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                processLogin();
            }
        });
        submit.onMouseClickedProperty().setValue(event -> processLogin());
    }

    private void processLogin() {
        if (!StringUtils.isBlank(userName.getText())) {
            String userId = userName.getText();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("chatClient.fxml"));
                fxmlLoader.setControllerFactory(param -> {
                    Optional<Constructor<?>> constructor = Arrays.stream(param.getConstructors())
                            .filter(constructor1 -> (constructor1.getParameterCount() == 1) &&
                                    (TransportLane.class.isAssignableFrom(constructor1.getParameterTypes()[0]))).findFirst();
                    if (constructor.isPresent()) {
                        try {
                            return constructor.get().newInstance(transportLaneFactory.createLane(userId));
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            log.error("Unable to create controller", e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        return null;
                    }
                });
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 500, 800);
                scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
                ChatClient.primaryStage.setTitle(userId);
                ChatClient.primaryStage.setScene(scene);
                ChatClient.primaryStage.show();
            } catch (IOException e) {
                log.error("Error while opening chat client ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
