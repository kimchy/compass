/*
 * Copyright 2004-2006 the original author or authors.
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

import org.compass.core.util.ClassUtils;
import org.compass.core.util.JdkVersion;

/**
 * @author kimchy
 */
public abstract class CompassConfigurationFactory {

    private static final String DEFAULT_COMPASS_CONFIG = "org.compass.core.config.CompassConfiguration";

    private static final String ANNOTATIONS_COMPASS_CONFIG = "org.compass.core.config.CompassAnnotationsConfiguration";

    public static CompassConfiguration newConfiguration() throws ConfigurationException {
        String compassConfigurationClassName = DEFAULT_COMPASS_CONFIG;
        if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
            compassConfigurationClassName = ANNOTATIONS_COMPASS_CONFIG;
        }
        Class compassConfigurationClass;
        CompassConfiguration config;
        try {
            compassConfigurationClass = ClassUtils.forName(compassConfigurationClassName);
        } catch (ClassNotFoundException e) {
            try {
                compassConfigurationClass = ClassUtils.forName(DEFAULT_COMPASS_CONFIG);
            } catch (ClassNotFoundException e1) {
                throw new ConfigurationException("Failed to create configuration class ["
                        + compassConfigurationClassName + "] and default [" + DEFAULT_COMPASS_CONFIG + "]", e);
            }
        }
        try {
            config = (CompassConfiguration) compassConfigurationClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create configuration class ["
                    + compassConfigurationClass.getName() + "]", e);
        }
        return config;
    }
}
