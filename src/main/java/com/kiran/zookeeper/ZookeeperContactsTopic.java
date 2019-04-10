package com.kiran.zookeeper;

import com.kiran.ContactsTopic;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by Kiran Kolli on 02-04-2019.
 */
public class ZookeeperContactsTopic implements ContactsTopic, Watcher {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperContactsTopic.class);

    private final String nodeName;
    private final ZooKeeper zooKeeper;
    private final Object lock = new Object();
    private final Set<Contact> contacts = new HashSet<>();
    private final Map<String, Subscriber> subscribers = new HashMap<>();
    private final Executor updatesExecutor = Executors.newSingleThreadExecutor();

    public ZookeeperContactsTopic(String nodeName, String hostPort) {
        this.nodeName = nodeName;
        try {
            this.zooKeeper = new ZooKeeper(hostPort, (int) TimeUnit.SECONDS.toMillis(10), event -> log.info("Zookeeper event: {}", event));
            this.zooKeeper.getChildren(nodeName, this);
        } catch (IOException | KeeperException | InterruptedException e) {
            log.error("Error while connecting to zookeeper", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            List<String> users = zooKeeper.getChildren(this.nodeName, this);
            updatesExecutor.execute(() -> informChanges(users));
        } catch (KeeperException e) {
            log.error("Error while watching /chatUsers", e);
        } catch (InterruptedException e) {
            log.error("Error while watching /chatUsers", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        log.info("Received subscription request for : {}", subscriber.getId());
        try {
            zooKeeper.create(this.nodeName + "/" + subscriber.getId().getUserId(), new byte[0], OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            log.info("Node created");
        } catch (KeeperException | InterruptedException e) {
            log.error("Unable to publish arrival of subscriber {}", subscriber.getId().getUserId(), e);
            //throw new RuntimeException(e);
        }
        synchronized (lock) {
            subscribers.putIfAbsent(subscriber.getId().getUserId(), subscriber);
        }
    }

    public void cleanUp() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("Unable to close zookeeper cleanly", e);
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    private void informChanges(List<String> users) {
        Set<Contact> currentSet = users.stream().map(Contact::new).collect(Collectors.toSet());
        log.info("All Children : {}", currentSet);
        Set<Contact> added;
        Set<Contact> deleted;
        synchronized (lock) {
            Set<Contact> copy = new HashSet<>(currentSet);
            currentSet.removeAll(contacts);
            contacts.removeAll(copy);
            added = new HashSet<>(currentSet);
            deleted = new HashSet<>(contacts);
            contacts.removeAll(deleted);
            contacts.addAll(added);
        }
        log.info("Added users: {}", added);
        log.info("Deleted users: {}", deleted);
        log.info("Contacts list: {}", contacts);
        subscribers.values().forEach(subscriber -> {
            added.forEach(contact -> subscriber.onContactAdded(contact));
            deleted.forEach(contact -> subscriber.onContactDeleted(contact));
        });
    }
}
