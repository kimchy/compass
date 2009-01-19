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
 * Specifies a searchable property on property or field of the {@link Searchable} class.
 *
 * <p>The searchable property will automatically create a {@link SearchableMetaData},
 * with its name being the field/property name. It will not be created if the
 * {@link #name()} is not set AND there are either {@link SearchableMetaData} or
 * {@link SearchableMetaDatas} annotating the class field/property. Most of
 * the attributes that can control the meta-data are provided in the searchable
 * property as well, they are marked in the java doc.
 *
 * <p>The searchable property/meta-data is meant to handle basic types (which usually translate to
 * a String saved in the search engine). The conversion is done using converters, with
 * Compass providing converters for most basic types. A specialized Converter can be
 * associated with the auto generated meta-data using {@link #converter()}. The
 * specialized converter will implement the {@link org.compass.core.converter.Converter}
 * interface, usually extending the {@link org.compass.core.converter.basic.AbstractBasicConverter}.
 *
 * <p>Another way of defining a converter for a class can be done using the {@link SearchableClassConverter}
 * to annotate the class that needs conversion, with Compass auto detecting it.
 *
 * <p>Note, that most of the time, a specialized converter for user classes will not be needed,
 * since the {@link SearchableComponent} usually makes more sense to use.
 *
 * <p>The searchable property can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable property
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link #type()} should be used to hint for the collection
 * element type.
 *
 * <p>The searchable property can annotate an array/collections as well, with the array element type used for
 * Converter lookups.
 *
 * <p>Compass might require an internal meta-data to be created, so it can identify the correct
 * value that match the property/field. Controlling the creation and specifics of the intenal
 * meta-data id can be done using {@link #managedId()} and {@link #managedIdIndex()}.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableProperty {

    /**
     * Controls if the internal meta-data id creation.
     */
    ManagedId managedId() default ManagedId.NA;

    /**
     * If the internal meta-data id is created, controls it's
     * index parameter.
     */
    ManagedIdIndex managedIdIndex() default ManagedIdIndex.NA;

    /**
     * The class type of the property. Mainly used for <code>Collection</code> properties, without
     * specific Generic type parameter.
     */
    Class type() default Object.class;

    /**
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

    /**
     * Converter that will apply to the property mapping. Not the generated
     * meta-data.
     */
    String propertyConverter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     *
     * <p>It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";

    // Generated MetaData definitions

    /**
     * The name of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#name()}.
     * If no value is defined, will default to the class field/property name.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    String name() default "";

    /**
     * The boost of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#boost()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    float boost() default 1.0f;

    /**
     * The store of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#store()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Store store() default Store.NA;

    /**
     * The index of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#index()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Index index() default Index.NA;

    /**
     * The termVector of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#termVector()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    TermVector termVector() default TermVector.NA;

    /**
     * The omitNorms of the auto generated {@link SearchableMetaData}. Maps to
     * {@link SearchableMetaData#omitNorms()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    OmitNorms omitNorms() default OmitNorms.NA;

    /**
     * The omitTf of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#omitTf()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    OmitTf omitTf() default OmitTf.NA;

    /**
     * The reverse of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#reverse()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Reverse reverse() default Reverse.NO;

    /**
     * The analyzer of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#analyzer()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    String analyzer() default "";

    /**
     * The exclude from all of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#excludeFromAll()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    ExcludeFromAll excludeFromAll() default ExcludeFromAll.NO;

    /**
     * The converter of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#converter()}.
     * The meta-data will be auto generated only if the name has a value.
     *
     * <p>This converter will also be used for an internal meta-data id (if required to be
     * generated).
     */
    String converter() default "";

    /**
     * The format of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#format()}.
     * The meta-data will be auto generated only if the name has a value.
     *
     * <p>This format will also be used for an internal meta-data id (if required to be
     * generated).
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
