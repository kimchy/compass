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
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;

/**
 * Goes over all the {@link org.compass.core.mapping.ResourceMapping} and if no sub index hashing is set
 * for them, will set a {@link org.compass.core.engine.subindex.ConstantSubIndexHash} based on the alias name.
 *
 * @author kimchy
 */
public class SubIndexHashMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        for (ResourceMapping resourceMapping : compassMapping.getRootMappings()) {
            if (resourceMapping.getSubIndexHash() == null) {
                ((InternalResourceMapping) resourceMapping).setSubIndexHash(new ConstantSubIndexHash(resourceMapping.getAlias()));
            }
        }
        return compassMapping;
    }
}