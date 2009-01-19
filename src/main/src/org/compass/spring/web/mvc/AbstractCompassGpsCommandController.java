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

package org.compass.spring.web.mvc;

import org.compass.gps.CompassGps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * A general base class for Spring's MVC Command Controllers that use
 * <code>CompassGps</code>.
 * <p>
 * Expects <code>CompassGps</code> instance to be injected, and provides
 * access to it.
 * 
 * @author kimchy
 */
public abstract class AbstractCompassGpsCommandController extends AbstractCommandController implements InitializingBean {

    private CompassGps compassGps;

    public void afterPropertiesSet() throws Exception {
        if (compassGps == null) {
            throw new IllegalArgumentException("Must set the compassGpd property");
        }
    }

    /**
     * Returns the <code>CompassGps</code>.
     * 
     * @return The Compass Gps
     */
    public CompassGps getCompassGps() {
        return compassGps;
    }

    /**
     * Sets the <code>CompassGps</code>.
     * 
     * @param compassGps
     */
    public void setCompassGps(CompassGps compassGps) {
        this.compassGps = compassGps;
    }

}
