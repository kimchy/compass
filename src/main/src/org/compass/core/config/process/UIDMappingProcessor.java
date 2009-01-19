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

package org.compass.core.config.process;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;

/**
 * Goes over all the root resource mappings and update the path where the internal UID is stored.
 *
 * @author kimchy
 * @see org.compass.core.mapping.internal.InternalResourceMapping#setUIDPath(String)
 */
public class UIDMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        String uidProperty = namingStrategy.buildPath(namingStrategy.getRootPath(), "uid").getPath().intern();

        ResourceMapping[] rootMappings = compassMapping.getRootMappings();
        for (ResourceMapping rootMapping : rootMappings) {
            ((InternalResourceMapping) rootMapping).setUIDPath(uidProperty);
        }
        return compassMapping;
    }
}
