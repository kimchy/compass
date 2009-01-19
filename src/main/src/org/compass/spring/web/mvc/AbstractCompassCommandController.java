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

import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * A general base class for Spring's MVC Command Controllers that use
 * <code>Compass</code>.
 * <p>
 * Expects <code>Compass</code> instance to be injected, and provides access
 * to both <code>Compass</code> and <code>CompassTemplate</code> instances.
 * 
 * @author kimchy
 */
public abstract class AbstractCompassCommandController extends AbstractCommandController implements InitializingBean {

    private Compass compass;

    private CompassTemplate compassTemplate;

    public void afterPropertiesSet() throws Exception {
        if (compass == null) {
            throw new IllegalArgumentException("Must set compass property");
        }
        this.compassTemplate = new CompassTemplate(compass);
    }

    /**
     * Returns <code>Compass</code> instance.
     * 
     * @return The compass instance.
     */
    public Compass getCompass() {
        return compass;
    }

    /**
     * Sets the <code>Compass</code> instance.
     * 
     * @param compass
     */
    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    /**
     * Returns a <code>CompassTemplate</code> that wraps the injected
     * <code>Compass</code> instance.
     * 
     * @return Compass template that wraps the injected Compass instance.
     */
    protected CompassTemplate getCompassTemplate() {
        return this.compassTemplate;
    }
}
