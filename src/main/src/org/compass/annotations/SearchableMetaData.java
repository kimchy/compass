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
 * Sepcifies additional meta-data on a {@link SearchableProperty} or
 * {@link SearchableId}.
 *
 * <p>{@link SearchableId} and {@link SearchableProperty} can be used to
 * auto-generate meta-data. If more than one meta-data is required, the
 * {@link SearchableMetaData}/{@link SearchableMetaDatas} can be used.
 *
 * <p>The searchable meta-data is meant to handle basic types (which usually translate to
 * a String saved in the search engine). The conversion is done using converters, with
 * Compass providing converters for most basic types. A specialized Converter can be
 * associated with the meta-data using {@link #converter()}. The
 * specialized converter will implement the {@link org.compass.core.converter.Converter}
 * interface, usually extending the {@link org.compass.core.converter.basic.AbstractBasicConverter}.
 *
 * <p>Another way of defining a converter for a class can be done using the {@link SearchableClassConverter}
 * to annotate the class that needs conversion, with Compass auto detecting it.
 *
 * <p>Note, that most of the time, a specialized converter for user classes will not be needed,
 * since the {@link SearchableComponent} usually makes more sense to use.
 *
 * <p>The searchalbe property can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable property
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link SearchableProperty#type()} should be used to hint for
 * the collection element type.
 *
 * <p>The searchable property can annotate an array as well, with the array element type used for
 * Converter lookups.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableMetaData {

    /**
     * The name of the meta-data. The name will be the value the contend will
     * be saved under, so it can later be searched using "author:london" (here
     * the name is author).
     */
    String name();

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
     * Specifies whether and how the meta-data proeprty should value will be revered.
     */
    Reverse reverse() default Reverse.NO;

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
     * The converter lookup name for the given meta-data.
     * <p/>
     * Defaults to one of Compass internal converters.
     */
    String converter() default "";

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    String format() default "";

    /**
     * A null value to use to store in the index when the property has a <code>null</code>
     * value. Defaults to not storing null values if the globabl setting of
     * <code>compass.mapping.nullvalue</code> is not set. If it set, disabling the null
     * value can be done by setting it to {@link org.compass.core.config.CompassEnvironment.NullValue#DISABLE_NULL_VALUE_FOR_MAPPING}
     * value (<code>$disable$</code>).
     */
    String nullValue() default "";

    /**
     * Should this propety be included in the spell check index.
     *
     * <p>Note, most times this is not requried to be configured, since by default, the
     * spell check index uses the "all" property.
     */
    SpellCheck spellCheck() default SpellCheck.EXCLUDE;
}
