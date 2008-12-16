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

import org.compass.core.Property;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.internal.DefaultAllMapping;

/**
 * @author kimchy
 */
public class ResourceAllMappingBuilder {

    final DefaultAllMapping mapping;

    ResourceAllMappingBuilder(DefaultAllMapping mapping) {
        this.mapping = mapping;
    }

    public ResourceAllMappingBuilder enable(boolean enable) {
        mapping.setSupported(enable);
        return this;
    }

    public ResourceAllMappingBuilder excludeAlias(boolean excludeAlias) {
        mapping.setExcludeAlias(excludeAlias);
        return this;
    }

    public ResourceAllMappingBuilder includePropertiesWithNoMappings(boolean includePropertiesWithNoMappings) {
        mapping.setIncludePropertiesWithNoMappings(includePropertiesWithNoMappings);
        return this;
    }

    public ResourceAllMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    public ResourceAllMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    public ResourceAllMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    public ResourceAllMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}
