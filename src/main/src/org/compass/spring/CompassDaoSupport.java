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

package org.compass.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient super class for Compass data access objects.
 * <p>
 * Requires either a <code>Compass</code> or a <code>CompassTemplate</code>
 * to be set.
 * <p>
 * The base class is intended for CompassTemplate usage, and it creates a new
 * CompassTemplate if a Compass parameter is set.
 * 
 * @author kimchy
 */
public abstract class CompassDaoSupport implements InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private CompassTemplate compassTemplate;

    /**
     * Sets the Compass to be used by this DAO. Will automatically create a
     * CompassTemplate based on the given Compass.
     * 
     * @param compass
     */
    public final void setCompass(Compass compass) {
        this.compassTemplate = createCompassTemplate(compass);
    }

    /**
     * Creates a new {@link CompassTemplate} based on the provided {@link Compass}. Subclasses
     * can override it to create a subclass of CompassTemplate, or change the configuration.
     * <p>
     * The method is only called when initializing the dao with Compass instance
     * and not a CompassTemplate instance.
     * 
     * @param compass
     * @return A new {@link CompassTemplate} warpping the given {@link Compass} instance.
     */
    protected CompassTemplate createCompassTemplate(Compass compass) {
        return new CompassTemplate(compass);
    }

    /**
     * Returns the Compass used by the DAO.
     * 
     * @return compass instance used by the DAO
     */
    public final Compass getCompass() {
        return (this.compassTemplate != null) ? this.compassTemplate.getCompass() : null;
    }

    /**
     * Sets the CompassTemplate used by the DAO explicitly. It is an alternative
     * to setting the Compass directly.
     * 
     * @param compassTemplate
     */
    public final void setCompassTemplate(CompassTemplate compassTemplate) {
        this.compassTemplate = compassTemplate;
    }

    /**
     * Returns the CompassTemplate for this DAO. Pre-initialized with Compass or
     * set explicitly.
     * 
     * @return The compass template for the DAO
     */
    public final CompassTemplate getCompassTemplate() {
        return compassTemplate;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.compassTemplate == null) {
            throw new IllegalArgumentException("compass or compassTemplate property is required");
        }
        initDao();
    }

    protected void initDao() throws Exception {

    }
}
