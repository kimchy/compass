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

package org.compass.core.mapping.osem.builder;

import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.osem.IdComponentMapping;

/**
 * @author kimchy
 */
public class ClassIdComponentMappingBuilder {

    final IdComponentMapping mapping;

    public ClassIdComponentMappingBuilder(String name) {
        mapping = new IdComponentMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    public ClassIdComponentMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    public ClassIdComponentMappingBuilder maxDepth(int maxDepth) {
        mapping.setMaxDepth(maxDepth);
        return this;
    }

    public ClassIdComponentMappingBuilder prefix(String prefix) {
        mapping.setPrefix(prefix);
        return this;
    }

    public ClassIdComponentMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public ClassIdComponentMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    public ClassIdComponentMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }
}