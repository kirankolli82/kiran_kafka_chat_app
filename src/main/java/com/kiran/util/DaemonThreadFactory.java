package com.kiran.util;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Kiran Kolli on 04-04-2019.
 */
public class DaemonThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
