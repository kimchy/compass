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

import org.compass.core.mapping.osem.ClassPropertyAnalyzerController;

/**
 * A builder allowing to construct a class analyzer property mapping. Class analyzer property mapping
 * allows to dynamically define the analyzer that will be used to analyzer the class (properties that are
 * specificed as analyzed). The value of the analyzer property will be used to lookup a registered analyzer
 * within Compass. If no analyzer is found, the {@link #nullAnalyzer(String)} will be used (if specified).
 *
 * @author kimchy
 * @see OSEM#analyzer(String)
 * @see SearchableMappingBuilder#add(SearchableAnalyzerMappingBuilder) 
 */
public class SearchableAnalyzerMappingBuilder {

    final ClassPropertyAnalyzerController mapping;

    /**
     * Constructs a new Class analyzer property using the provided name.
     */
    public SearchableAnalyzerMappingBuilder(String name) {
        this.mapping = new ClassPropertyAnalyzerController();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(true);
    }

    /**
     * The name of the analyzer that will be used if the property has the null value.
     */
    public SearchableAnalyzerMappingBuilder nullAnalyzer(String nullAnalyzer) {
        mapping.setNullAnalyzer(nullAnalyzer);
        return this;
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableAnalyzerMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }
    
    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableAnalyzerMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }
}