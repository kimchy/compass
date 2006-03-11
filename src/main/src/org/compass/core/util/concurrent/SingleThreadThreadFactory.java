package org.compass.core.util.concurrent;

import org.compass.core.util.backport.java.util.concurrent.ThreadFactory;

/**
 */
public class SingleThreadThreadFactory implements ThreadFactory {

    private String name;

    private boolean daemon;

    public SingleThreadThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(null, r, name);
        t.setDaemon(daemon);
        return t;
    }
}
