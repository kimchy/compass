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

package org.compass.core.mapping.osem.internal;

import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.mapping.osem.ObjectMapping;

/**
 * @author kimchy
 */
public interface InternalObjectMapping extends ObjectMapping, InternalOsemMapping {

    /**
     * Sets the accessor type for this mapping. The accessor type can be
     * field, property or a custom implementation of {@link org.compass.core.accessor.PropertyAccessor}
     * (this can be either the FQN of the class name or a regsitered type in the configuration, see
     * {@link org.compass.core.accessor.PropertyAccessorFactory}.
     */
    void setAccessor(String accessor);

    /**
     * Sets the class property name of the object mapping.
     */
    void setPropertyName(String propertyName);

    /**
     * Sets which alias (or if not present, the FQN of the class name)
     * this object property is defined at.
     */
    void setDefinedInAlias(String alias);

    /**
     * Sests the getter for the property.
     */
    void setGetter(Getter getter);

    /**
     * Sets the setter for the property.
     */
    void setSetter(Setter setter);
}
