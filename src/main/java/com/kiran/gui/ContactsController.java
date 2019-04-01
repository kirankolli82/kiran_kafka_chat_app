package com.kiran.gui;

import com.kiran.ContactsTopic;
import com.kiran.TransportLane;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Kiran Kolli on 01-04-2019.
 */
public class ContactsController {

    private static Logger log = LoggerFactory.getLogger(ContactsController.class);

    @FXML
    private VBox contactsBox;

    private final ContactsTopic topic;
    private final ContactsTopic.Contact currentUser;
    private final TransportLaneFactory transportLaneFactory;

    ContactsController(ContactsTopic topic, String userId, TransportLaneFactory transportLaneFactory) {
        this.topic = topic;
        this.currentUser = new ContactsTopic.Contact(userId);
        this.transportLaneFactory = transportLaneFactory;
    }

    @FXML
    public void initialize() {
        this.topic.subscribe(new ContactsTopic.Subscriber() {
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
    }

    private void openChatWith(ContactsTopic.Contact contact) {
        if (contact != null) {
            String userId = contact.getUserId();
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
                Stage newStage = new Stage();
                newStage.setTitle(userId);
                newStage.setScene(scene);
                newStage.show();
            } catch (IOException e) {
                log.error("Error while opening chat client ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
