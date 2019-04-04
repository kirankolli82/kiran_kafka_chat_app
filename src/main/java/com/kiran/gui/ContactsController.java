package com.kiran.gui;

import com.kiran.ContactsTopic;
import com.kiran.TransportLaneFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Kiran Kolli on 01-04-2019.
 */
public class ContactsController {

    private static Logger log = LoggerFactory.getLogger(ContactsController.class);

    @FXML
    private VBox contactsBox;

    private final ContactsTopic contactsTopic;
    private final ContactsTopic.Contact currentUser;
    private final TransportLaneFactory.TransportLane transportLane;
    private final Map<String, ChatClientController> controllersByUserId = new HashMap<>();

    ContactsController(ContactsTopic topic, String userId, TransportLaneFactory transportLaneFactory) {
        this.contactsTopic = topic;
        this.currentUser = new ContactsTopic.Contact(userId);
        this.transportLane = transportLaneFactory.createLaneForUser(userId);
    }

    @FXML
    public void initialize() {
        this.contactsTopic.subscribe(new ContactsTopic.Subscriber() {
            @Override
            public ContactsTopic.Contact getId() {
                return currentUser;
            }

            @Override
            public void onContactAdded(ContactsTopic.Contact contact) {
                if (!Objects.equals(contact, currentUser)) {
                    Platform.runLater(() -> {
                        Label label = new Label(contact.getUserId());
                        label.getStyleClass().setAll("lbl", "lbl-success");
                        label.setOnMouseClicked(event -> {
                            if (Objects.equals(MouseButton.PRIMARY, event.getButton())) {
                                if (event.getClickCount() == 2) {
                                    openChatWith(contact);
                                }
                            }
                        });
                        contactsBox.getChildren().add(label);
                    });
                }
            }

            @Override
            public void onContactDeleted(ContactsTopic.Contact contact) {
                Platform.runLater(() -> {
                    List<Node> toRemove = contactsBox.getChildren().stream()
                            .filter(node -> node instanceof Label)
                            .map(node -> (Label) node)
                            .filter(label -> Objects.equals(label.getText(), contact.getUserId()))
                            .collect(Collectors.toList());
                    contactsBox.getChildren().removeAll(toRemove);
                });
            }
        });

        this.transportLane.subscribeToMessages((from, message) -> Platform.runLater(() -> {
            log.info("Got message: {}; from: {}", message, from);
            ChatClientController chatClientController = controllersByUserId.get(from.getUserId());
            if (chatClientController == null) {
                ChatClientController clientController = controllersByUserId.computeIfAbsent(from.getUserId(), key -> new ChatClientController(from, transportLane));
                openChatWindow(from);
                clientController.onMessageReceived(message);
            } else {
                chatClientController.onMessageReceived(message);
            }
        }));
    }

    private void openChatWith(ContactsTopic.Contact contact) {
        if (contact != null) {
            openChatWindow(contact);
        }
    }

    private void openChatWindow(ContactsTopic.Contact contact) {
        try {
            log.info("Opening chat window with {}", contact);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("chatClient.fxml"));
            fxmlLoader.setControllerFactory(param -> controllersByUserId.computeIfAbsent(contact.getUserId(), key -> new ChatClientController(contact, transportLane)));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 500, 800);
            scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
            Stage newStage = new Stage();
            newStage.setTitle(contact.getUserId());
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            log.error("Error while opening chat client ", e);
            throw new RuntimeException(e);
        }
    }
}
