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

package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For {@link Searchable} classes, allows to control the "all" meta-data
 * definitions per searchable class.
 *
 * <p>The "all" meta-data is an internal meta-data, which holds
 * searchable information of all the class searchable content.
 *
 * <p>The definitions here are per searchable class definitions. For global
 * control of the "all" meta-data see {@link org.compass.core.config.CompassEnvironment.All}
 * settings.
 *
 * @author kimchy
 * @see Searchable
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableAllMetaData {

    /**
     * The name of the "all" meta-data that will be created.
     * Defaults to the global setting.
     */
    String name() default "";

    /**
     * Controls if the searchable class will create it's own internal "all"
     * meta-data. The "all" meta-data holds searchable information of all
     * the class searchable content.
     *
     * <p>If using the "all" meta-data, it can be controlled using the
     * {@link SearchableAllMetaData} annotation.
     */
    EnableAll enable() default EnableAll.NA;

    /**
     * The term vector for the "all" meta-data.
     */
    TermVector termVector() default TermVector.NO;

    /**
     * Controls is the alias will be stored within the "all" proeprty or not.
     */
    ExcludeAlias excludeAlias() default ExcludeAlias.NA;

    /**
     * Set to <code>TRUE</code> if when adding the different {@link org.compass.core.Resource} properties,
     * properties with no mappings will be added to the "all" property. A resoruce can have property with no
     * mappings if it was added programtically to the resource.
     *
     * <p>See {@link org.compass.core.config.CompassEnvironment.All#INCLUDE_UNMAPPED_PROPERTIES} for system wide
     * setting and teh defult value if this is set to <code>NA</code>.
     */
    NABoolean includePropertiesWithNoMappings() default NABoolean.NA;
    
    /**
     * Expert:
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    OmitNorms omitNorms() default OmitNorms.NO;

    /**
     * Expert:
     * If set, omit tf from postings of this indexed field.
     */
    OmitTf omitTf() default OmitTf.NO;
    
    /**
     * Should this propety be included in the spell check index.
     *
     * <p>Note, most times this is not requried to be configured, since by default, the
     * spell check index uses the "all" property.
     */
    SpellCheck spellCheck() default SpellCheck.EXCLUDE;
}
