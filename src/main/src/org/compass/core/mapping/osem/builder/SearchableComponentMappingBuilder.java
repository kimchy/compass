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
import org.compass.core.mapping.osem.ComponentMapping;

/**
 * @author kimchy
 */
public class SearchableComponentMappingBuilder {

    final ComponentMapping mapping;

    public SearchableComponentMappingBuilder(String name) {
        mapping = new ComponentMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    public SearchableComponentMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    public SearchableComponentMappingBuilder maxDepth(int maxDepth) {
        mapping.setMaxDepth(maxDepth);
        return this;
    }

    public SearchableComponentMappingBuilder prefix(String prefix) {
        mapping.setPrefix(prefix);
        return this;
    }

    public SearchableComponentMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public SearchableComponentMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    public SearchableComponentMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }
}
