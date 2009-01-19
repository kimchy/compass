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

package org.compass.core.mapping;

import org.compass.core.Property;

/**
 * A set of settings associated with the all mapping.
 *
 * @author kimchy
 */
public interface AllMapping {

    /**
     * Returns <code>true</code> if the "all" property will be created for the resource. The
     * all property is a special property that have all the other resource values in it to be
     * searchable.
     */
    Boolean isSupported();

    /**
     * Should the alias be excluded from the "all" property. Default should be <code>false</code>.
     */
    Boolean isExcludeAlias();

    /**
     * Returns <code>true</code> if when adding the different {@link org.compass.core.Resource} properties,
     * properties with no mappings will be added to the "all" property. A resoruce can have property with no
     * mappings if it was added programtically to the resource.
     */
    Boolean isIncludePropertiesWithNoMappings();

    /**
     * Returns the name of the all property for the given resoruce.
     */
    String getProperty();

    /**
     * Returns the term vector configuration for the given all proeprty.
     */
    Property.TermVector getTermVector();

    /**
     * Expert:
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    Boolean isOmitNorms();

    Boolean isOmitTf();

    /**
     * 
     */
    SpellCheck getSpellCheck();

    /**
     * Returns a copy of the All mapping settings.
     */
    AllMapping copy();
}
