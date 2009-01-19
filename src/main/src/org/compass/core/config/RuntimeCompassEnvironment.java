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

package org.compass.core.config;

/**
 * A set of settings constants that applies on the Compass session level.
 *
 * @author kimchy
 * @see org.compass.core.CompassSession#getSettings()
 */
public abstract class RuntimeCompassEnvironment {

    /**
     * Settings for cascading operations.
     */
    public abstract class Cascade {

        /**
         * Disable all cascading operations.
         */
        public static final String DISABLE = CompassEnvironment.Cascade.DISABLE;
    }
}
