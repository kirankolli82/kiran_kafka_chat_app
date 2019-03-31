package com.kiran.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kiran Kolli on 31-03-2019.
 */
public class ZookeeperDemo {

    private static Logger log = LoggerFactory.getLogger(ZookeeperDemo.class);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 100, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                log.info("Path: {}", event.getPath());
                log.info("Type: {}", event.getType());
                log.info("State: {}", event.getState().name());
                log.info("Wrapper Path: {}", event.getWrapper().getPath());
            }
        });

        List<String> children = zooKeeper.getChildren("/chatUsers", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    log.info("Children: {}", zooKeeper.getChildren("/chatUsers", this));
                } catch (KeeperException | InterruptedException e) {
                    log.error("Error while watching /chatUsers", e);
                }
            }
        });

        log.info("Children: {}", children);
        Thread.sleep(TimeUnit.MINUTES.toMillis(10));
    }
}
