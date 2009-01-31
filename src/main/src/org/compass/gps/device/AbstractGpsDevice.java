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

package org.compass.gps.device;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGps;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;
import org.compass.gps.MirrorDataChangesGpsDevice;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * A general abstract device which can be used by all types of devices.
 * <p/>
 * Provides support for device name, state management
 * {@link AbstractGpsDevice#isRunning()}, as well as general helper methods.
 *
 * @author kimchy
 */
public abstract class AbstractGpsDevice implements CompassGpsDevice {

    protected Log log = LogFactory.getLog(getClass());

    private String name;

    protected CompassGpsInterfaceDevice compassGps;

    private volatile boolean started = false;

    private boolean internalMirrorDataChanges = false;

    private String[] filteredEntitiesForIndex;

    private Set filteredEntitiesLookupForIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompassGps getGps() {
        return compassGps;
    }

    public void injectGps(CompassGps compassGps) {
        this.compassGps = (CompassGpsInterfaceDevice) compassGps;
    }

    public void setFilteredEntitiesForIndex(String[] filteredEntitiesForIndex) {
        this.filteredEntitiesForIndex = filteredEntitiesForIndex;
    }

    public String buildMessage(String message) {
        return "{" + name + "}: " + message;
    }

    public boolean isFilteredForIndex(String entity) {
        return (filteredEntitiesLookupForIndex != null && filteredEntitiesLookupForIndex.contains(entity));
    }

    public synchronized void index(final IndexPlan indexPlan) throws CompassGpsException {
        if (!isRunning()) {
            throw new IllegalStateException(
                    buildMessage("must be running in order to perform the index operation"));
        }
        compassGps.executeForIndex(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                doIndex(session, indexPlan);
                session.flush();
            }
        });
    }

    /**
     * Derived devices must implement the method to perform the actual indexing
     * operation.
     */
    protected abstract void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException;

    public synchronized void start() throws CompassGpsException {
        if (name == null) {
            throw new IllegalArgumentException("Must set the name for the device");
        }
        if (compassGps == null) {
            throw new IllegalArgumentException(
                    buildMessage("Must set the compassGps for the device, or add it to an active CompassGps instance"));
        }
        if (!started) {
            if (this instanceof MirrorDataChangesGpsDevice) {
                internalMirrorDataChanges = ((MirrorDataChangesGpsDevice) this).isMirrorDataChanges();
            }
            // build the filtered enteties map
            if (filteredEntitiesForIndex != null) {
                filteredEntitiesLookupForIndex = new HashSet();
                for (int i = 0; i < filteredEntitiesForIndex.length; i++) {
                    filteredEntitiesLookupForIndex.add(filteredEntitiesForIndex[i]);
                }
            }
            doStart();
            started = true;
        }
    }

    /**
     * Derived devices can implement it in case of start event notification.
     *
     * @throws CompassGpsException
     */
    protected void doStart() throws CompassGpsException {

    }

    public synchronized void stop() throws CompassGpsException {
        if (started) {
            doStop();
            started = false;
        }
    }

    /**
     * Derived devices can implement it in case of stop event notification.
     *
     * @throws CompassGpsException
     */
    protected void doStop() throws CompassGpsException {

    }

    /**
     * A no op. Subclasses should overide only if needed.
     */
    public void refresh() throws CompassGpsException {

    }

    public boolean isRunning() {
        return started;
    }

    public boolean isPerformingIndexOperation() {
        return compassGps.isPerformingIndexOperation();
    }

    public boolean shouldMirrorDataChanges() {
        if (!internalMirrorDataChanges) {
            return false;
        }
        return isRunning();
    }
}
