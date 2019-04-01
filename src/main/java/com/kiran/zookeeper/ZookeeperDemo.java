package com.kiran.zookeeper;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by Kiran Kolli on 31-03-2019.
 */
public class ZookeeperDemo {

    private static Logger log = LoggerFactory.getLogger(ZookeeperDemo.class);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 100, event -> log.info("Event: {}", event));

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
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        zooKeeper.create("/chatUsers/kiran", new byte[0], OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        zooKeeper.close();
    }
}
