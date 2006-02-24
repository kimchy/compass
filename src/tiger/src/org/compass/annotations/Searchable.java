/*
 * Copyright 2004-2006 the original author or authors.
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
 * Marks a class as searchable.
 * A searchable class is assoiated with an alias, and allows to perform full
 * text search on it's mapped properties/fields.
 * <p/>
 * The searchable class is associated with an alias, which can be used to
 * reference the class when performing search operations, or for other
 * mappings to extend it.
 * <p/>
 * A class mapping has it's own fully functional index, unless using the
 * {@link #subIndex()} to join several searchable classes into the same
 * index (when joining several searchalbe classes into the same index,
 * the search will be much faster, but updates perform locks on the sub index
 * level, so it might slow it down).
 * <p/>
 * A searchable class creates an internal "all" meta-data, which holds
 * searchable information of all the class searchable content. You can
 * control if it will be created or not using {@link #all()}, and control
 * the "all" property using the {@link SearchableAllMetaData} annotation.
 * <p/>
 * A searchable class can have constant meta-data associated with it. They
 * can be defined using the {@link SearchableConstant} and the {@link SearchableConstants}.
 * <p/>
 * Searchable class can have annotations defined on either it's fields, or
 * on the field getter accessor. The possible annotions for them are:
 * {@link SearchableId}, {@link SearchableProperty}, {@link SearchableComponent},
 * and {@link SearchableReference}. Note that collections are automatically
 * detected and handled by Compass if annotated.
 * <p/>
 * If the searchable class extends a class, or implement intefaces, they
 * will be automatically detected and added to it in a revrse order. If the
 * same annotaion is defined in both the searcable class and one of it's
 * super class / interfaces, it will be overriden unless defined otherwise.
 * The annotaions will be included even if the inteface/superclass do not
 * implement the {@link Searchable} annotation.
 * <p/>
 * The seachable class can have a specialized analyzer (different from the
 * default one) associated with it using {@link #analyzer()}. Note, that this
 * will associate the class statically with an analyzer. Dynamically associating
 * the class with an analyzer, the {@link SearchableAnalyzerProperty} can be
 * used to annotated the dynamic value for the analyzer to use.
 * <p/>
 * The searchable class can extend other mappings defined elsewhere (either by
 * the xml mappings, or annotations). Remember, that the searchable class will
 * already include all the annotations in it's super class or interface (recursivly).
 * So there is no need to specify them in {@link #extend()}. Note, that xml mapping
 * <code>contract</code>s can be extended as well.
 * <p/>
 * By default, the searchable class is defined as a root class. A root class is
 * a top level searchable class. A non root class can be used to define mappings
 * definitions for {@link SearchableComponent}, and it is preferable that classes
 * that are only used as component mapping definitions, will be defined with {@link #root()}
 * <code>false</code>.
 * <p/>
 * The {@link #poly()} can be used to mapped polymprphic inheritance tree. This is the less
 * prefable way to map an inhertiance tree, since the fact that a searchable class automatically
 * inhertis all it's base class and interface mappings, means that the same result can be
 * acheived by marking the all the inheritance tree classes as {@link Searchable}.
 * <p/>
 * Compass provides it's own internal converter for searchable classes
 * {@link org.compass.core.converter.mapping.osem.ClassMappingConverter}. For advance usage,
 * the converter can be set using {@link #converter()} IT will convert
 * the {@link org.compass.core.mapping.osem.ClassMapping} definitions.
 *
 * @author kimchy
 * @see SearchableId
 * @see SearchableProperty
 * @see SearchableComponent
 * @see SearchableReference
 * @see SearchableAnalyzerProperty
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    /**
     * The alias that is associated with the class. Can be used to refernce
     * the searchable class when performing search operations, or for other
     * mappings to extend it.
     * <p/>
     * Default value is the short name of the class.
     */
    String alias() default "";

    /**
     * The sub index the searchable class will be saved to. A sub index is
     * a fully functional index.
     * <p/>
     * When joining several searchalbe classes into the same index,
     * the search will be much faster, but updates perform locks on the sub index
     * level, so it might slow it down.
     * <p/>
     * Defaults to the searchable class {@link #alias()} value.
     */
    String subIndex() default "";

    /**
     * Boost level for the searchable class. Controls the ranking of hits
     * when performing searches.
     */
    float boost() default 1.0f;

    /**
     * Controls if the searchable class will create it's own internal "all"
     * meta-data. The "all" meta-data holds searchable information of all
     * the class searchable content.
     * <p/>
     * If using the "all" meta-data, it can be controlled using the
     * {@link SearchableAllMetaData} annotation.
     */
    boolean all() default true;

    /**
     * Defines if the searchable class is a root class. A root class is a top
     * level searchable class. You should define the searchable class with <code>false</code>
     * if it only acts as mapping definitions for a {@link SearchableComponent}.
     */
    boolean root() default true;

    /**
     * Used to mapped polymprphic inheritance tree. This is the less prefable way to map
     * an inheritance tree, since the fact that a searchable class automatically
     * inhertis all it's base class and interface mappings, means that the same result can be
     * acheived by marking the all the inheritance tree classes as {@link Searchable}, in a
     * more performant way.
     */
    boolean poly() default false;

    /**
     * A specialized analyzer (different from the default one) associated with the
     * searchable class. Note, that this will associate the class statically with
     * an analyzer. Dynamically associating the class with an analyzer can be done
     * by using the {@link SearchableAnalyzerProperty} to use the property value
     * to dynamically lookup the value for the analyzer to use.
     */
    String analyzer() default "";

    /**
     * A list of aliases to extend. Extending the aliases allows to include other
     * mapping definitions, defined via annotations or xml.
     * <p/>
     * Remember, that the searchable class will already include all the annotations
     * in it's super class or interface (recursivly). So there is no need to specify
     * them in {@link #extend()}. Note, that xml mapping code>contract</code>s can
     * be extended as well.
     */
    String[] extend() default {};

    /**
     * Allows to set a converter for the {@link org.compass.core.mapping.osem.ClassMapping} of
     * the searchable class.
     * <p/>
     * This is advance setting, since Compass comes with it's own internal
     * {@link org.compass.core.converter.mapping.osem.ClassMappingConverter}.
     */
    String converter() default "";
}