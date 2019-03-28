package com.kiran.gui;

import com.kiran.TransportLane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;


/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public class ChatClientController {

    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;
    @FXML
    private VBox messageBox;

    private TransportLane transportLane;


    @FXML
    public void initialize() {
        sendButton.setOnMouseClicked(event -> {
            String message = textArea.getText();
            if (!StringUtils.isBlank(message)) {
                CompletableFuture<Void> result = transportLane.sendOnLane(message);
                result.whenComplete((aVoid, throwable) -> {
                    if (throwable == null) {
                        Platform.runLater(() -> {
                            Label messageLabel = new Label(message);
                            messageLabel.getStyleClass().setAll("lbl", "lbl-primary");
                            messageLabel.setTextAlignment(TextAlignment.RIGHT);
                            messageBox.getChildren().add(messageLabel);
                        });
                    }
                });
            }
        });
    }
}
