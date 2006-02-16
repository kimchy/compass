/*
 * Copyright 2004-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.transaction;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;

/**
 * @author kimchy
 */
public abstract class TransactionSessionManager {

    private static final Log log = LogFactory.getLog(TransactionSessionManager.class);

    private static final ThreadLocal holders = new ThreadLocal();

    public static CompassSessionHolder getHolder(Compass compass) {
        Map map = (Map) holders.get();
        if (map == null) {
            return null;
        }
        CompassSessionHolder holder = (CompassSessionHolder) map.get(compass);
        if (holder != null && log.isDebugEnabled()) {
//            log.debug("Retrieved holder [" + holder + "] for compass [" + compass + "] bound to thread ["
//                    + Thread.currentThread().getName() + "]");
        }
        return holder;
    }

    public static void bindHolder(Compass compass, CompassSessionHolder compassSessionHolder)
            throws IllegalStateException {
        Map map = (Map) holders.get();
        if (map == null) {
            map = new HashMap();
            holders.set(map);
        }
        if (map.containsKey(compass)) {
            throw new IllegalStateException("Already holder [" + map.get(compass) + "] for compass [" + compass
                    + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        map.put(compass, compassSessionHolder);
        if (log.isDebugEnabled()) {
//            log.debug("Bound holder [" + compassSessionHolder + "] for compass [" + compass + "] to thread ["
//                    + Thread.currentThread().getName() + "]");
        }
    }

    public static CompassSessionHolder unbindHolder(Compass compass) {
        Map map = (Map) holders.get();
        if (map == null || !map.containsKey(compass)) {
            throw new IllegalStateException("No value for compass [" + compass + "] bound to thread ["
                    + Thread.currentThread().getName() + "]");
        }
        CompassSessionHolder holder = (CompassSessionHolder) map.remove(compass);
        // remove entire ThreadLocal if empty
        if (map.isEmpty()) {
            holders.set(null);
        }
        if (log.isDebugEnabled()) {
//            log.debug("Removed holder [" + holder + "] for compass [" + compass + "] from thread ["
//                    + Thread.currentThread().getName() + "]");
        }
        return holder;
    }

}
