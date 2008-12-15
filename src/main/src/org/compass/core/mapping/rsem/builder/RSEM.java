/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.mapping.rsem.builder;

import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.DefaultContractMapping;
import org.compass.core.mapping.rsem.RawBoostPropertyMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyAnalyzerController;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;

/**
 * @author kimchy
 */
public abstract class RSEM {

    private RSEM() {

    }

    public static ResourceContractMappingBuilder contract(String alias) {
        DefaultContractMapping mapping = new DefaultContractMapping();
        mapping.setAlias(alias);
        return new ResourceContractMappingBuilder(mapping);
    }

    public static ResourceMappingBuilder resource(String alias) {
        RawResourceMapping mapping = new RawResourceMapping();
        mapping.setAlias(alias);
        mapping.setRoot(true);
        return new ResourceMappingBuilder(mapping);
    }

    public static ResourceIdMappingBuilder id(String name) {
        RawResourcePropertyIdMapping mapping = new RawResourcePropertyIdMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setOmitNorms(true);
        mapping.setOmitTf(true);
        return new ResourceIdMappingBuilder(mapping);
    }

    public static ResourcePropertyMappingBuilder property(String name) {
        RawResourcePropertyMapping mapping = new RawResourcePropertyMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        return new ResourcePropertyMappingBuilder(mapping);
    }

    public static ResourceAnalyzerMappingBuilder analyzer(String name) {
        RawResourcePropertyAnalyzerController mapping = new RawResourcePropertyAnalyzerController();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        return new ResourceAnalyzerMappingBuilder(mapping);
    }

    public static ResourceBoostMappingBuilder boost(String name) {
        RawBoostPropertyMapping mapping = new RawBoostPropertyMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        return new ResourceBoostMappingBuilder(mapping);
    }

    public static ResourceAllMappingBuilder all() {
        DefaultAllMapping allMapping = new DefaultAllMapping();
        return new ResourceAllMappingBuilder(allMapping);
    }
}
