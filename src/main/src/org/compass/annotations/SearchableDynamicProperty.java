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
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableDynamicProperty {

    /**
     * A prefix that will be prepended to the dynamic proeprty names that will be created.
     */
    String namePrefix() default "";

    /**
     * When the name is a custom object, the name property can get field/property from it and use it
     * as the resource property name. Note, the field/property can be marked using {@link org.compass.annotations.SearchableDynamicName}
     * annotation as well.
     */
    String nameProperty() default "";

    /**
     * When the name is a custom object, the value property can get field/property from it and use it
     * as the resource property value. Note, the field/property can be marked using {@link org.compass.annotations.SearchableDynamicValue}
     * annotation as well.
     */
    String valueProperty() default "";

    /**
     * The format to apply to the name. Only applies to format-able converters
     * (like dates and numbers).
     */
    String nameFormat() default "";

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    String valueFormat() default "";

    /**
     * Explicitly set the name converter lookup name that will be used to convert the name. Note, most times
     * (for example, with Map and generics) the type and converter can be automatically detected.
     */
    String nameConverter() default "";

    /**
     * Explicitly set the value converter lookup name that will be used to convert the value. Note, most times
     * (for example, with Map and generics) the type and converter can be automatically detected.
     */
    String valueConverter() default "";

    /**
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

    /**
     * The property accessor that will be fetch and write the property value.
     *
     * <p>It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";

    /**
     * The boost level for the meta-data. Will cause hits
     * based on this meta-data to rank higher.
     */
    public abstract float boost() default 1.0f;

    /**
     * Specifies whether and how a meta-data property will be stored.
     */
    public abstract Store store() default Store.NA;

    /**
     * Specifies whether and how a meta-data proeprty should be indexed.
     */
    public abstract Index index() default Index.NA;

    /**
     * Specifies whether and how a meta-data property should have term vectors.
     */
    public abstract TermVector termVector() default TermVector.NA;

    /**
     * Expert:
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    public abstract OmitNorms omitNorms() default OmitNorms.NA;

    /**
     * Expert:
     * If set, omit tf from postings of this indexed field.
     */
    public abstract OmitTf omitTf() default OmitTf.NA;

    /**
     * The converter lookup name for the given meta-data.
     * <p/>
     * Defaults to one of Compass internal converters.
     */
    public abstract String converter() default "";

    /**
     * A null value to use to store in the index when the property has a <code>null</code>
     * value. Defaults to not storing null values if the globabl setting of
     * <code>compass.mapping.nullvalue</code> is not set. If it set, disabling the null
     * value can be done by setting it to {@link org.compass.core.config.CompassEnvironment.NullValue#DISABLE_NULL_VALUE_FOR_MAPPING}
     * value (<code>$disable$</code>).
     */
    public abstract String nullValue() default "";
}