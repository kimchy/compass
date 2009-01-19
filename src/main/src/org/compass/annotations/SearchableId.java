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
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Specifies a searchable id on property or field of the {@link Searchable} class.
 *
 * <p>Each searchable class must have at least one id annotation. The type
 * of the field/property can be a simple type, or a custom class. In case
 * of a custom class, there should be a specialized converter associtaed
 * with it, with the preferable way of defining the converter is to use
 * the {@link SearchableClassConverter} annotating the custom class.
 *
 * <p>A searchable class can have more than one searchable id associated with it.
 *
 * <p>For simple usage, the annotation can create a {@link SearchableMetaData} automatically
 * (without explicitly defining one on the field/property).
 * It will only be created if the {@link #name()} is set to a value. Most of
 * the attributes that can control the meta-data are provided in the searchable
 * id as well, they are marked in the java doc.
 *
 * <p>Note, that if the {@link #name()} is not set, no defined {@link SearchableMetaData}
 * will be created, and Compass will end up generating an internal meta-data for it.
 * For additional meta-datas, use the {@link SearchableMetaData} or {@link SearchableMetaDatas}.
 *
 * <p>Compass might require an internal meta-data to be created, so it can identify the correct
 * value that match the property/field. Controlling the creation and specifics of the intenal
 * meta-data id can be done using {@link #managedId()} and {@link #managedIdIndex()}.
 *
 * @author kimchy
 * @see Searchable
 * @see SearchableClassConverter
 * @see SearchableMetaData
 * @see SearchableMetaDatas
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RUNTIME)
public @interface SearchableId {

    /**
     * Controls if the internal meta-data id creation.
     */
    ManagedId managedId() default ManagedId.AUTO;

    /**
     * If the internal meta-data id is created, controls it's
     * index parameter.
     */
    ManagedIdIndex managedIdIndex() default ManagedIdIndex.NA;

    /**
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

    /**
     * Converter that will apply to the id mapping
     * ({@link org.compass.core.mapping.osem.ClassIdPropertyMapping}).
     * Defaults to the {@link org.compass.core.mapping.osem.ClassPropertyMapping}.
     */
    String idConverter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     * <p/>
     * It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";

    // Generated MetaData definitions

    /**
     * The name of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#name()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    String name() default "";

    /**
     * The boost of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#boost()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    float boost() default 1.0f;

    /**
     * The store of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#store()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    Store store() default Store.NA;

    /**
     * The index of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#index()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    Index index() default Index.NA;

    /**
     * The termVector of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#termVector()}.
     *
     * <p>The meta-data will be auto generated only if the name has a value.
     */
    TermVector termVector() default TermVector.NA;

    /**
     * The termVector of the auto generated {@link SearchableMetaData}. Maps to
     * {@link SearchableMetaData#omitNorms()}.
     *
     * <p>The meta-data will be auto generated only if the name has a value.
     */
    OmitNorms omitNorms() default OmitNorms.YES;

    /**
     * Expert:
     * If set, omit tf from postings of this indexed field.
     */
    OmitTf omitTf() default OmitTf.YES;

    /**
     * The reverse of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#reverse()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    Reverse reverse() default Reverse.NO;

    /**
     * The analyzer of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#analyzer()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    String analyzer() default "";

    /**
     * The execlude from all of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#excludeFromAll()}.
     * <p/>
     * The meta-data will be auto generated only if the name has a value.
     */
    ExcludeFromAll excludeFromAll() default ExcludeFromAll.NO;

    /**
     * The converter of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#converter()}.
     * The meta-data will be auto generated only if the name has a value.
     * <p>
     * This converter will also be used for an internal meta-data id (if required to be
     * generated).
     */
    String converter() default "";

    /**
     * The format of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#format()}.
     * The meta-data will be auto generated only if the name has a value.
     * <p>
     * This format will also be used for an internal meta-data id (if required to be
     * generated).
     */
    String format() default "";

    /**
     * Should this propety be included in the spell check index.
     *
     * <p>Note, most times this is not requried to be configured, since by default, the
     * spell check index uses the "all" property.
     */
    SpellCheck spellCheck() default SpellCheck.EXCLUDE;
}
