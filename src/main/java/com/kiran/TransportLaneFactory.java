package com.kiran;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public interface TransportLaneFactory {

    TransportLane createLaneForUser(String userId);

    interface Subscriber {
        void processMessage(ContactsTopic.Contact from, String message);
    }

    interface TransportLane {

        ContactsTopic.Contact getId();

        void subscribeToMessages(Subscriber subscriber);

        CompletableFuture<Void> sendOnLane(ContactsTopic.Contact to, String message);
    }

}
