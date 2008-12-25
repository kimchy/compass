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

import org.compass.core.mapping.osem.ClassBoostPropertyMapping;

/**
 * Allows to dynamically define the boost value of the resource based on a Class property value.
 *
 * @author kimchy
 * @see OSEM#boost(String)
 */
public class ClassBoostMappingBuilder {

    final ClassBoostPropertyMapping mapping;

    /**
     * Constructs a new boost JSON property mapping.
     */
    public ClassBoostMappingBuilder(String name) {
        this.mapping = new ClassBoostPropertyMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(false);
    }

    /**
     * The default boost value that will be used of the JSON property to be used
     * has <code>null</code> value. Defaults to <code>1.0f</code>.
     */
    public ClassBoostMappingBuilder defaultBoost(float defaultBoost) {
        mapping.setDefaultBoost(defaultBoost);
        return this;
    }

    public ClassBoostMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }
}