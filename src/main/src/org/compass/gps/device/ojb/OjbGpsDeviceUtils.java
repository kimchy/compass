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

package org.compass.gps.device.ojb;

import org.apache.ojb.broker.PersistenceBroker;
import org.compass.gps.CompassGpsDevice;

/**
 * Sets of utilies to hadle the persistence broker and ojb device.
 * 
 * @author kimchy
 * 
 */
public abstract class OjbGpsDeviceUtils {

    /**
     * Attaches the persistence broker for batch indexing operation to the
     * device.
     */
    public static void attachPersistenceBrokerForMirror(CompassGpsDevice device, PersistenceBroker persistenceBroker)
            throws NonOjbDeviceException {
        if (!(device instanceof OjbGpsDevice)) {
            throw new NonOjbDeviceException("The device is not an ojb device");
        }

        ((OjbGpsDevice) device).attachLifecycleListeners(persistenceBroker);
    }

    /**
     * Removes the ojb device as a lifecycle listener from the persistence
     * broker.
     */
    public static void removePersistenceBrokerForMirror(CompassGpsDevice device, PersistenceBroker persistenceBroker)
            throws NonOjbDeviceException {
        if (!(device instanceof OjbGpsDevice)) {
            throw new NonOjbDeviceException("The device is not an ojb device");
        }

        ((OjbGpsDevice) device).removeLifecycleListeners(persistenceBroker);
    }

    /**
     * Attach the ojb device as a lifecycle listener to the persistence broker.
     */
    public static void attachPersistenceBrokerForIndex(CompassGpsDevice device, PersistenceBroker persistenceBroker)
            throws NonOjbDeviceException {

        if (!(device instanceof OjbGpsDevice)) {
            throw new NonOjbDeviceException("The device is not an ojb device");
        }

        ((OjbGpsDevice) device).setIndexPersistenceBroker(persistenceBroker);
    }
}
