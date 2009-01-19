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
 * A dynamic meta data evaluation of the given expression using an expression
 * language library.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableDynamicMetaData {

    /**
     * The name of the meta-data. The name will be the value the contend will
     * be saved under, so it can later be searched using "author:london" (here
     * the name is author).
     */
    String name();

    /**
     * The expression to be evaluated by the dynamic expression evaluation library.
     */
    String expression();

    /**
     * The dynamic converter lookup name. Compass built in ones include: jexl, velocity.
     */
    String converter();

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
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    String format() default "";

    /**
     * If using a format, the type of the expression result.
     */
    Class type() default Object.class;

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
