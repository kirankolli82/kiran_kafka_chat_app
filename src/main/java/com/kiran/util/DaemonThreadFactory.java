package com.kiran.util;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Kiran Kolli on 04-04-2019.
 */
public class DaemonThreadFactory implements ThreadFactory {

    private String name;

    public DaemonThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(this.name);
        thread.setDaemon(true);
        return thread;
    }
}
