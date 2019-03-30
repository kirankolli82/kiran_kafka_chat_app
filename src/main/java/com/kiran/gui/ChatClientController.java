package com.kiran.gui;

import com.kiran.TransportLane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;


/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public class ChatClientController {

    private static Logger log = LoggerFactory.getLogger(ChatClientController.class);

    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;
    @FXML
    private VBox messageBox;

    private final TransportLane transportLane;

    public ChatClientController(TransportLane transportLane) {
        this.transportLane = transportLane;
    }


    @FXML
    public void initialize() {
        messageBox.setSpacing(15D);
        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                processText();
            }
        });

        sendButton.setOnMouseClicked(event -> processText());
    }

    private void processText() {
        String message = textArea.getText();
        if (!StringUtils.isBlank(message)) {
            CompletableFuture<Void> result = transportLane.sendOnLane(message);
            result.whenComplete((aVoid, throwable) -> {
                if (throwable == null) {
                    Platform.runLater(() -> {
                        HBox hBox = new HBox();
                        hBox.setAlignment(Pos.CENTER_RIGHT);
                        Label messageLabel = new Label(message);
                        messageLabel.getStyleClass().setAll("lbl", "lbl-primary");
                        messageLabel.setTextAlignment(TextAlignment.RIGHT);
                        HBox.setHgrow(messageLabel, Priority.ALWAYS);
                        hBox.getChildren().add(messageLabel);
                        messageBox.getChildren().add(hBox);
                        textArea.clear();
                    });
                }
            });
        }
    }
}
