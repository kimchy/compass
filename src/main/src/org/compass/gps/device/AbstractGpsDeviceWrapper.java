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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.gps.CompassGps;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;

/**
 * A helper base class that can wrap a {@link org.compass.gps.CompassGpsDevice}
 * and delegate the calls defined at the
 * {@link org.compass.gps.CompassGpsDevice} interface.
 *
 * @author kimchy
 */
public class AbstractGpsDeviceWrapper implements CompassGpsDevice {

    protected Log log = LogFactory.getLog(getClass());

    protected CompassGpsDevice gpsDevice;

    public void setGpsDevice(CompassGpsDevice gpsDevice) {
        this.gpsDevice = gpsDevice;
    }

    public CompassGps getGps() {
        checkDeviceSet();
        return this.gpsDevice.getGps();
    }

    public void injectGps(CompassGps compassGps) {
        checkDeviceSet();
        this.gpsDevice.injectGps(compassGps);
    }

    public String getName() {
        checkDeviceSet();
        return gpsDevice.getName();
    }

    public void setName(String name) {
        checkDeviceSet();
        this.gpsDevice.setName(name);
    }

    public void start() throws CompassGpsException {
        checkDeviceSet();
        this.gpsDevice.start();
    }

    public void stop() throws CompassGpsException {
        checkDeviceSet();
        this.gpsDevice.stop();
    }

    public void refresh() throws CompassGpsException {
        checkDeviceSet();
        this.gpsDevice.refresh();
    }

    public void index(IndexPlan indexPlan) throws CompassGpsException, IllegalStateException {
        checkDeviceSet();
        this.gpsDevice.index(indexPlan);
    }

    public boolean isRunning() {
        checkDeviceSet();
        return this.gpsDevice.isRunning();
    }

    public boolean isPerformingIndexOperation() {
        checkDeviceSet();
        return this.gpsDevice.isPerformingIndexOperation();
    }

    public boolean shouldMirrorDataChanges() {
        checkDeviceSet();
        return this.gpsDevice.shouldMirrorDataChanges();
    }

    protected void checkDeviceSet() {
        if (this.gpsDevice == null) {
            throw new IllegalStateException("Must set the wrapped device first");
        }
    }
}
