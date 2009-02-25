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

import java.util.ArrayList;

import org.compass.core.config.process.*;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;

/**
 * @author kimchy
 */
public class CompassMappingProcessor implements MappingProcessor {

    private ArrayList<MappingProcessor> mappingProcessors = new ArrayList<MappingProcessor>();

    public CompassMappingProcessor() {
        mappingProcessors.add(new SubIndexHashMappingProcessor());
        mappingProcessors.add(new ResolveExtendsMappingProcessor());
        mappingProcessors.add(new PropertyAccessorMappingProcessor());
        mappingProcessors.add(new NullValueMappingProcessor());
        mappingProcessors.add(new DynamicPropertyProcessor());
        mappingProcessors.add(new ResolveRefAliasProcessor());
        mappingProcessors.add(new CollectionMappingProcessor());
        mappingProcessors.add(new ResolveLateAttributesPreLateBindingMappingProcessor());
        mappingProcessors.add(new ConverterLookupMappingProcessor());
        // start late binding
        mappingProcessors.add(new LateBindingOsemMappingProcessor());
        mappingProcessors.add(new LateBindingXsemMappingProcessor());
        mappingProcessors.add(new LateBindingJsonMappingProcessor());
        // end late binding
        mappingProcessors.add(new CascadingMappingProcessor());
        mappingProcessors.add(new InternalIdsMappingProcessor());
        mappingProcessors.add(new PostProcessorMappingProcessor());
        mappingProcessors.add(new RootAliasPostProcessor());
        mappingProcessors.add(new CompassMappingPostProcessor());
        mappingProcessors.add(new ResolveLateAttributesPostLateBindingMappingProcessor());
        mappingProcessors.add(new UIDMappingProcessor());
        mappingProcessors.add(new ValidatorMappingProcessor());
    }

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws ConfigurationException {
        CompassMapping retMapping = compassMapping;
        for (MappingProcessor mappingProcessor : mappingProcessors) {
            retMapping = mappingProcessor.process(retMapping, namingStrategy, converterLookup, settings);
        }
        return retMapping;
    }
}
