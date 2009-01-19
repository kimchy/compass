/*
 * Copyright 2004-2009 the original author or authors.
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

package org.hibernate.search.event;

import java.util.Map;
import java.util.Properties;

import org.compass.gps.device.hibernate.embedded.CompassEventListener;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRemoveEventListener;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;

/**
 * Allows to hack automatic support for Compass in Hibernate when used with Hiberante annotations.
 *
 * @author kimchy
 */
public class EventListenerRegister {

    /**
     * Add the FullTextIndexEventListener to all listeners, if enabled in configuration
     * and if not already registered.
     *
     * @param listeners
     * @param properties the Search configuration
     */
    public static void enableHibernateSearch(EventListeners listeners, Properties properties) {
        boolean foundCompass = false;
        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(CompassEventListener.COMPASS_PREFIX) || key.startsWith(CompassEventListener.COMPASS_GPS_INDEX_PREFIX)) {
                foundCompass = true;
                break;
            }
        }
        if (foundCompass) {
            CompassEventListener.log.debug("Found Compass settings, enabling Compass listener");
        } else {
            CompassEventListener.log.debug("No Compass properties found, disabling Compass listener");
            return;
        }
        final CompassEventListener searchListener = new CompassEventListener();
        // PostInsertEventListener
        listeners.setPostInsertEventListeners(
                addIfNeeded(
                        listeners.getPostInsertEventListeners(),
                        searchListener,
                        new PostInsertEventListener[]{searchListener}
                )
        );
        // PostUpdateEventListener
        listeners.setPostUpdateEventListeners(
                addIfNeeded(
                        listeners.getPostUpdateEventListeners(),
                        searchListener,
                        new PostUpdateEventListener[]{searchListener}
                )
        );
        // PostDeleteEventListener
        listeners.setPostDeleteEventListeners(
                addIfNeeded(
                        listeners.getPostDeleteEventListeners(),
                        searchListener,
                        new PostDeleteEventListener[]{searchListener}
                )
        );

        // PostCollectionRecreateEventListener
        listeners.setPostCollectionRecreateEventListeners(
                addIfNeeded(
                        listeners.getPostCollectionRecreateEventListeners(),
                        searchListener,
                        new PostCollectionRecreateEventListener[]{searchListener}
                )
        );
        // PostCollectionRemoveEventListener
        listeners.setPostCollectionRemoveEventListeners(
                addIfNeeded(
                        listeners.getPostCollectionRemoveEventListeners(),
                        searchListener,
                        new PostCollectionRemoveEventListener[]{searchListener}
                )
        );
        // PostCollectionUpdateEventListener
        listeners.setPostCollectionUpdateEventListeners(
                addIfNeeded(
                        listeners.getPostCollectionUpdateEventListeners(),
                        searchListener,
                        new PostCollectionUpdateEventListener[]{searchListener}
                )
        );

    }

    /**
     * Verifies if a Search listener is already present; if not it will return
     * a grown address adding the listener to it.
     *
     * @param <T>                 the type of listeners
     * @param listeners
     * @param searchEventListener
     * @param toUseOnNull         this is returned if listeners==null
     * @return
     */
    private static <T> T[] addIfNeeded(T[] listeners, T searchEventListener, T[] toUseOnNull) {
        if (listeners == null) {
            return toUseOnNull;
        } else if (!isPresentInListeners(listeners)) {
            return appendToArray(listeners, searchEventListener);
        } else {
            return listeners;
        }
    }

    /**
     * Will add one element to the end of an array.
     *
     * @param <T>        The array type
     * @param listeners  The original array
     * @param newElement The element to be added
     * @return A new array containing all listeners and newElement.
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] appendToArray(T[] listeners, T newElement) {
        int length = listeners.length;
        T[] ret = (T[]) java.lang.reflect.Array.newInstance(
                listeners.getClass().getComponentType(), length + 1
        );
        System.arraycopy(listeners, 0, ret, 0, length);
        ret[length] = newElement;
        return ret;
    }

    /**
     * Verifies if a FullTextIndexEventListener is contained in the array.
     *
     * @param listeners
     * @return true if it is contained in.
     */
    @SuppressWarnings("deprecation")
    private static boolean isPresentInListeners(Object[] listeners) {
        for (Object eventListener : listeners) {
            if (FullTextIndexEventListener.class == eventListener.getClass()) {
                return true;
            }
            if (FullTextIndexCollectionEventListener.class == eventListener.getClass()) {
                return true;
            }
        }
        return false;
    }
}
