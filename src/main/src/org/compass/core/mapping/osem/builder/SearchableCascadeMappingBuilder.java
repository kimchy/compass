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
import org.compass.core.mapping.osem.PlainCascadeMapping;

/**
 * Allows to define cascading annotation which will result in certain operations done on the object
 * that holds the property to be cascaded to its referenced objects.
 *
 * <p>Note, this is used when there are not component/reference mappings for the specified field/property
 * but still, cascading should still be performed.
 *
 * @author kimchy
 * @see OSEM#cascade(String)
 * @see SearchableMappingBuilder#add(SearchableCascadeMappingBuilder)
 */
public class SearchableCascadeMappingBuilder {

    final PlainCascadeMapping mapping;

    /**
     * Constrcuts a new cascade mapping builder for the specified class property.
     */
    public SearchableCascadeMappingBuilder(String name) {
        mapping = new PlainCascadeMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setCascades(new Cascade[]{Cascade.ALL});
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableCascadeMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableCascadeMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * The operations that will cascade to the target association. Defaults all operations
     * being cascaded.
     */
    public SearchableCascadeMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }
}
