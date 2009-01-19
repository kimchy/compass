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

import java.util.Iterator;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.MultipleMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;

/**
 * <p>Goes through each {@link org.compass.core.mapping.osem.ClassMapping} and handles its
 * null value definition. If {@link org.compass.core.config.CompassEnvironment.NullValue#NULL_VALUE}
 * is set, will use it to set all unset null values. If it is not set, will leave the null
 * values as is.
 *
 * <p>In case it is set, then will disable null value for mappings that have null value of
 * {@link org.compass.core.config.CompassEnvironment.NullValue#DISABLE_NULL_VALUE_FOR_MAPPING}
 * value.
 *
 * @author kimchy
 */
public class NullValueMappingProcessor implements MappingProcessor {

    private String globalNullValue;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        globalNullValue = settings.getSetting(CompassEnvironment.NullValue.NULL_VALUE);

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            processMapping(aliasMapping);
        }
        return compassMapping;
    }

    private void processMapping(Mapping mapping)
            throws MappingException {
        if ((mapping instanceof InternalResourcePropertyMapping)) {
            InternalResourcePropertyMapping resourcePropertyMapping = (InternalResourcePropertyMapping) mapping;
            if (!resourcePropertyMapping.hasNullValue()) {
                resourcePropertyMapping.setNullValue(globalNullValue);
            } else if (CompassEnvironment.NullValue.DISABLE_NULL_VALUE_FOR_MAPPING.equals(
                    resourcePropertyMapping.getNullValue())) {
                resourcePropertyMapping.setNullValue(null);
            }
        }

        if (mapping instanceof MultipleMapping) {
            MultipleMapping multipleMapping = (MultipleMapping) mapping;
            for (Iterator it = multipleMapping.mappingsIt(); it.hasNext();) {
                processMapping((Mapping) it.next());
            }
        }
    }
}