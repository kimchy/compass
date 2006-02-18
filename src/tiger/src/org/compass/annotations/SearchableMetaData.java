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
 * @author kimchy
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableMetaData {

    String name();

    float boost() default 1.0f;

    Store store() default Store.YES;

    Index index() default Index.TOKENIZED;

    TermVector termVector() default TermVector.NO;

    Reverse reverse() default Reverse.NO;

    String analyzer() default "";

    boolean exceludeFromAll() default false;

    /**
     * The converter lookup name.
     */
    String converter() default "";

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    String format() default "";
}
