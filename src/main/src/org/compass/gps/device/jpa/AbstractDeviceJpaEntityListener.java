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

package org.compass.gps.device.jpa;

import org.compass.core.Compass;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * An abstract support class for event lifecycle JPA spec support. Requires the <code>JpaGpsDevice<code>
 * instance to be provided (usual sub classes will simple fetch it from the Jndi location). This
 * is the least prefereable way to use lifecycle event listerens, please see
 * {@link JpaGpsDevice} and {@link org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjector}.
 *
 * @author kimchy
 */
public abstract class AbstractDeviceJpaEntityListener extends AbstractCompassJpaEntityListener {

    protected abstract JpaGpsDevice getDevice();

    protected Compass getCompass() {
        return ((CompassGpsInterfaceDevice) getDevice().getGps()).getMirrorCompass();
    }

    protected boolean disable() {
        return !getDevice().shouldMirrorDataChanges() || getDevice().isPerformingIndexOperation();
    }
}
