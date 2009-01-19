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

package org.compass.core.mapping.osem.builder;

import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.osem.ParentMapping;

/**
 * @author kimchy
 */
public class SearchableParentMappingBuilder {

    final ParentMapping mapping;

    public SearchableParentMappingBuilder(String name) {
        mapping = new ParentMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    public SearchableParentMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    public SearchableParentMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public SearchableParentMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }
}
