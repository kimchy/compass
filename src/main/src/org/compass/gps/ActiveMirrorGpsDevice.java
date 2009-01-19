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
 * An active mirror gps device, meaning that in order to perform the mirror
 * operation, one must actively call the {@link #performMirroring()} method.
 * <p>
 * Compass::Gps provides a way to schedule active mirror gps devices by using
 * the {@link org.compass.gps.device.ScheduledMirrorGpsDevice} gps device
 * wrapper.
 * 
 * @author kimchy
 * @see org.compass.gps.device.ScheduledMirrorGpsDevice
 */
public interface ActiveMirrorGpsDevice extends MirrorDataChangesGpsDevice {

    /**
     * Perform the actual data changes mirror operation. The operation will
     * detect all the changes made to the data source since the last
     * indexing/mirroring operation and reflect it to the index.
     * 
     * @throws CompassGpsException
     */
    void performMirroring() throws CompassGpsException;

}
