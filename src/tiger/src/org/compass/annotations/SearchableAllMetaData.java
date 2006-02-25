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
 * For {@link Searchable} classes, allows to control the "all" meta-data
 * definitions per searchable class.
 * <p/>
 * The "all" meta-data is an internal meta-data, which holds
 * searchable information of all the class searchable content.
 * <p/>
 * The definitions here are per searchable class definitions. For global
 * control of the "all" meta-data see {@link org.compass.core.config.CompassEnvironment.All}
 * settings.
 * <p/>
 * To enable or disable the "all" meta-data, see {@link Searchable#enableAll}.
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
     * The term vector for the "all" meta-data.
     */
    TermVector termVector() default TermVector.NO;

    /**
     * The analyzer that will be used on the "all" meta-data.
     */
    String analyzer() default "";
}
