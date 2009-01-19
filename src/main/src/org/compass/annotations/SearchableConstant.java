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
 * A constant meta-data that can be defined on a {@link Searchable} class.
 *
 * <p>A constant meta-data is a predefined name and value pair that will be
 * saved in the search engine index.
 *
 * <p>Multiple constants can be defined using the {@link SearchableConstants} annotation.
 *
 * @author kimchy
 * @see Searchable
 * @see SearchableConstants
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableConstant {

    /**
     * The name of the meta-data.
     */
    String name();

    /**
     * A list of values that the meta-data will have.
     */
    String[] values();

    /**
     * The boost level for the meta-data. Will cause hits
     * based on this meta-data to rank higher.
     */
    float boost() default 1.0f;

    /**
     * Specifies whether and how a meta-data property will be stored.
     */
    Store store() default Store.NA;

    /**
     * Specifies whether and how a meta-data proeprty should be indexed.
     */
    Index index() default Index.NA;

    /**
     * Specifies whether and how a meta-data property should have term vectors.
     */
    TermVector termVector() default TermVector.NA;

    /**
     * Expert:
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    OmitNorms omitNorms() default OmitNorms.NA;

    /**
     * Expert:
     * If set, omit tf from postings of this indexed field.
     */
    OmitTf omitTf() default OmitTf.NA;
    
    /**
     * Specifies a specialized analyzer lookup name that will be used to analyze
     * the meta-data content.
     * <p/>
     * Defaults to Compass default analyzer.
     */
    String analyzer() default "";

    /**
     * Specifies if this meta-data should be excluded from the generated
     * "all" meta-data.
     *
     * @see SearchableAllMetaData#enable()
     */
    ExcludeFromAll excludeFromAll() default ExcludeFromAll.NO;

    /**
     * Controls if the constant value should override the same constant defined
     * elsewhere for the same searchable class.
     */
    boolean override() default true;

    /**
     * Should this propety be included in the spell check index.
     *
     * <p>Note, most times this is not requried to be configured, since by default, the
     * spell check index uses the "all" property.
     */
    SpellCheck spellCheck() default SpellCheck.EXCLUDE;

    /**
     * Converter for the Constant meta-data mapping.
     */
    String converter() default "";
}
