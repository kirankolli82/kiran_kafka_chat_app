package com.kiran;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
public interface TransportLane {

    String getId();

    CompletableFuture<Void> sendOnLane(String message);

    String subscribeToMessages(Subscriber subscriber);

    void unsubscribe(String subscriberId);

    interface Subscriber extends Consumer<String> {
    }
}
