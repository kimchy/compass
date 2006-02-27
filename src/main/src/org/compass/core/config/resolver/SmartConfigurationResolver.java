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

package org.compass.core.config.resolver;

import java.io.File;
import java.net.URL;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;

/**
 * @author kimchy
 */
public class SmartConfigurationResolver implements ConfigurationResolver {

    public void configure(String resource, CompassConfiguration config) throws ConfigurationException {
        new DTDConfigurationResolver().configure(resource, config);
    }

    public void configure(URL url, CompassConfiguration config) throws ConfigurationException {
        new DTDConfigurationResolver().configure(url, config);
    }

    public void configure(File file, CompassConfiguration config) throws ConfigurationException {
        new DTDConfigurationResolver().configure(file, config);
    }
}
