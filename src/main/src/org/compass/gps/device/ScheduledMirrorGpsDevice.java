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
import org.compass.gps.ActiveMirrorGpsDevice;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.CompassGpsException;

/**
 * For {@link org.compass.gps.ActiveMirrorGpsDevice}s, the schedule mirror
 * device can call the
 * {@link org.compass.gps.ActiveMirrorGpsDevice#performMirroring()} in a
 * scheduled manner by wrapping the actual
 * {@link org.compass.gps.ActiveMirrorGpsDevice}.
 * <p/>
 * The schedule mirror gps device implements the
 * {@link org.compass.gps.ActiveMirrorGpsDevice} interface, and provides access
 * to the wrapped gps device using the {@link #getWrappedGpsDevice()}.
 * <p/>
 * For scheduling, the schedule mirror device uses the
 * <code>java.util.Timer</code>, and provides controll over the period,
 * daemon, and fixed rate parameters.
 *
 * @author kimchy
 */
public class ScheduledMirrorGpsDevice extends AbstractMirrorGpsDeviceWrapper implements ActiveMirrorGpsDevice {

    private static Log log = LogFactory.getLog(ScheduledMirrorGpsDevice.class);

    private ActiveMirrorGpsDevice gpsDevice;

    private MirrorGpsDeviceThread thread;

    private boolean daemon = true;

    private long period = 10000;

    /**
     * Creates a new instance. Note that the wrapped gps device must be set by
     * calling the <code>setGpsDevice</code> and it must implement the .
     */
    public ScheduledMirrorGpsDevice() {
    }

    /**
     * Creates a new instance of the scheduled device with the wrapped
     * {@link ActiveMirrorGpsDevice} initialized.
     *
     * @param gpsDevice
     */
    public ScheduledMirrorGpsDevice(ActiveMirrorGpsDevice gpsDevice) {
        this.gpsDevice = gpsDevice;
        setGpsDevice(gpsDevice);
    }

    /**
     * Checks that when setting the wrapped gps device, it is of type
     * {@link ActiveMirrorGpsDevice}
     */
    public void setGpsDevice(CompassGpsDevice gpsDevice) {
        if (!(gpsDevice instanceof ActiveMirrorGpsDevice)) {
            throw new IllegalArgumentException("The device must implement the ActiveMirrorGpsDevice interface");
        }
        this.gpsDevice = (ActiveMirrorGpsDevice) gpsDevice;
        super.setGpsDevice(gpsDevice);
    }

    /**
     * Starts the scheduled timer.
     */
    public synchronized void start() throws CompassGpsException {
        if (isRunning()) {
            throw new IllegalStateException("{" + getName() + "} Scheduled mirror device is already running");
        }
        if (log.isInfoEnabled()) {
            log.info("{" + getName() + "} Starting scheduled mirror device with period [" + period + "ms] daemon [" + daemon + "]");
        }
        this.gpsDevice.start();
        thread = new MirrorGpsDeviceThread(period);
        thread.setName("Compass Mirror Gps Device [" + getName() + "]");
        thread.start();
    }

    /**
     * Stops the scheduled timer.
     */
    public synchronized void stop() throws CompassGpsException {
        if (!isRunning()) {
            throw new IllegalStateException("{" + getName() + "} Scheduled mirror device is already running");
        }
        if (log.isInfoEnabled()) {
            log.info("{" + getName() + "} Stopping scheduled mirror device");
        }
        thread.cancel();
        thread = null;
        this.gpsDevice.stop();
    }

    /**
     * Returns the wrapped active mirror gps device.
     */
    public ActiveMirrorGpsDevice getWrappedGpsDevice() {
        return gpsDevice;
    }

    /**
     * Sets the wrapped gps device.
     */
    public void setWrappedGpsDevice(ActiveMirrorGpsDevice gpsDevice) {
        this.gpsDevice = gpsDevice;
        setGpsDevice(gpsDevice);
    }

    /**
     * Performs the actual mirror operation, delegating the action to the
     * wrapped gps device.
     */
    public void performMirroring() throws CompassGpsException {
        checkDeviceSet();
        this.gpsDevice.performMirroring();
    }

    /**
     * If the scheduled timer whould work as a daemon thread or not.
     */
    public boolean isDaemon() {
        return daemon;
    }

    /**
     * Sets if the scheduled timer would work as a daemon thread or not.
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * The period of the scheduled service in milli-seconds.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Sets the period of the scheduled service in milli-seconds.
     *
     * @param period
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    private class MirrorGpsDeviceThread extends Thread {

        private long period;

        private boolean canceled;

        public MirrorGpsDeviceThread(long period) {
            this.period = period;
        }


        public void run() {
            while (!Thread.interrupted() || !canceled) {
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    break;
                }
                try {
                    gpsDevice.performMirroring();
                } catch (Exception e) {
                    log.warn("Failed to perform gps device mirroring", e);
                }
            }
        }

        public void cancel() {
            this.canceled = true;
            this.interrupt();
        }
    }
}
