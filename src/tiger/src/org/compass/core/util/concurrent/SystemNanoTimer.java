package org.compass.core.util.concurrent;

import org.compass.core.util.backport.java.util.concurrent.helpers.NanoTimer;

/**
 * @author kimchy
 */
public class SystemNanoTimer implements NanoTimer {

    public long nanoTime() {
        return System.nanoTime();
    }
}
