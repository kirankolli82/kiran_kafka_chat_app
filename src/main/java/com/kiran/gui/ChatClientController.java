package com.kiran.gui;


import com.kiran.ContactsTopic;
import com.kiran.TransportLaneFactory;
import com.kiran.util.DaemonThreadFactory;
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

import java.util.concurrent.*;


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

    private final ContactsTopic.Contact other;
    private final TransportLaneFactory.TransportLane transportLane;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory());

    public ChatClientController(ContactsTopic.Contact other, TransportLaneFactory.TransportLane transportLane) {
        this.other = other;
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

    public void onMessageReceived(String message) {
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().setAll("lbl", "lbl-info");
            messageLabel.setTextAlignment(TextAlignment.LEFT);
            HBox.setHgrow(messageLabel, Priority.ALWAYS);
            hBox.getChildren().add(messageLabel);
            messageBox.getChildren().add(hBox);
        });
    }

    private void processText() {
        String message = textArea.getText();
        if (!StringUtils.isBlank(message)) {
            executorService.submit(() -> {
                CompletableFuture<Void> result = transportLane.sendOnLane(other, message);
                try {
                    result.get(5, TimeUnit.SECONDS);
                    processSuccessfulSend(message);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    processSendFailed(message, e);
                }
            });
        }
    }


    private void processSendFailed(String message, Throwable throwable) {
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            Label messageLabel = new Label("WARNING: Could not send message: \n" + message + ";\n due to :" + throwable.getMessage());
            messageLabel.getStyleClass().setAll("lbl", "lbl-danger");
            messageLabel.setTextAlignment(TextAlignment.RIGHT);
            HBox.setHgrow(messageLabel, Priority.ALWAYS);
            hBox.getChildren().add(messageLabel);
            messageBox.getChildren().add(hBox);
        });
    }

    private void processSuccessfulSend(String message) {
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
}
