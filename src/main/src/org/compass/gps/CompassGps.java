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
 * <code>CompassGps</code> is responsible for managing
 * {@link org.compass.gps.CompassGpsDevice}s. It can hold one or more
 * devices, and manage their lifecycle. It is also resposible for creating an
 * abstraction between devices and their repectice <code>Compass</code>
 * instances, as part of the internal contract between
 * <code>CompassGps</code> and its devices and should not be used by a
 * non-device code (see
 * {@link org.compass.gps.spi.CompassGpsInterfaceDevice}).
 * 
 * @author kimchy
 */
public interface CompassGps {

    /**
     * Adds a {@link CompassGpsDevice} to be managed.
     *
     * @param gpsDevice
     */
    void addGpsDevice(CompassGpsDevice gpsDevice);

    /**
     * Sets a list of {@link CompassGpsDevice}s that will be managed.
     *
     * @param devices
     */
    void setGpsDevices(CompassGpsDevice[] devices);

    /**
     * Start <code>CompassGps</code> (also starts all the devices).
     * 
     * @throws CompassGpsException
     */
    void start() throws CompassGpsException;

    /**
     * Stops <code>CompassGps</code> (also starts all the devices).
     * 
     * @throws CompassGpsException
     */
    void stop() throws CompassGpsException;

    /**
     * Return <code>true</code> is started.
     */
    boolean isRunning();

    /**
     * Retuns <code>true</code> if the devide performs the index operaiton.
     */
    boolean isPerformingIndexOperation();

    /**
     * Indexes all the different devices (by calling their respective
     * <code>index()</code> operation.
     *
     * <p>Similar to calling the {@link #index(IndexPlan)} with a new
     * instance of {@link DefaultIndexPlan} (and nothing else set).
     * 
     * @throws CompassGpsException
     */
    void index() throws CompassGpsException, IllegalStateException;

    /**
     * Index just the given types.
     */
    void index(Class ... types) throws CompassGpsException, IllegalStateException;

    /**
     * Index just the aliases.
     */
    void index(String ... aliases) throws CompassGpsException, IllegalStateException;

    /**
     * Indexes all the different devices based on the given index plan. The index
     * plan, by default, supports constraining the indexing process to specific
     * classes/aliases/sub indexes.
     */
    void index(IndexPlan indexPlan) throws CompassGpsException, IllegalStateException;
}
