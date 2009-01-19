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

package org.compass.gps;

/**
 * A Compass Gps Device is responsible for interacting with a data source and
 * reflecting it in a compass index. A data source can be a file system, or a
 * database through the use of ORM tool (like hibernate).
 *
 * <p>A device should be able to provide the ability to index the data source,
 * which usually means iterating through the device data and indexing it. It
 * might also provide "real time" monitoring of changes in the device, and
 * applying them to the index as well.
 *
 * <p>A CompassGpsDevice can not operate as a standalone, and must be a part of a
 * {@link org.compass.gps.CompassGps} instance (even if we have only one
 * device), since the device requires the Compass and the Batch Compass
 * instances in order to apply the changes to the index.
 *
 * <p>The device has a name associated with it, and the name must be unique among
 * all the devices within a single CompassGps instance.
 * 
 * @author kimchy
 */
public interface CompassGpsDevice {

    /**
     * Returns the name associated with the device.
     * 
     * @return The name of the device.
     */
    String getName();

    /**
     * Sets the name associated with the device.
     */
    void setName(String name);

    /**
     * Sets the CompassGps that manages the device. Optional, since if the
     * device is added to a CompassGps instance, it will be done automatically.
     */
    void injectGps(CompassGps compassGps);

    /**
     * Returns the CompassGps that manages the device.
     * 
     * @return compassGps
     */
    CompassGps getGps();

    /**
     * Starts the device. Optional, since it is preferable to manage the all the
     * devices through the CompassGps API.
     * 
     * @throws CompassGpsException
     */
    void start() throws CompassGpsException;

    /**
     * Stops the device. Optional, since it is preferable to manage the all the
     * devices through the CompassGps API.
     * 
     * @throws CompassGpsException
     */
    void stop() throws CompassGpsException;

    /**
     * Refresh the given device. Mainly used to denote changes in the underlying Compass
     * instance and that they should be taken into account (such as new mappings).
     */
    void refresh() throws CompassGpsException;

    /**
     * Returns if the device is running.
     * 
     * @return <code>true</code> if theh device is running
     */
    boolean isRunning();

    /**
     * Index the device using the given index plan
     * 
     * @throws CompassGpsException
     * @throws IllegalStateException
     */
    void index(IndexPlan indexPlan) throws CompassGpsException, IllegalStateException;

    /**
     * Retuns <code>true</code> if the devide performs the index operaiton.
     */
    public boolean isPerformingIndexOperation();

    /**
     * Returns <code>true</code> if the device is required to perform mirror operation.
     */
    boolean shouldMirrorDataChanges();
}
