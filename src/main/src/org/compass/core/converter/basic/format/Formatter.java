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

package org.compass.core.converter.basic.format;

import java.text.ParseException;

/**
 * A generic interface allowing to format objects to Strings and parse them from Strings.
 *
 * @author kimchy
 */
public interface Formatter {

    /**
     * Formats the given object to String.
     */
    String format(Object obj);

    /**
     * Parse the given string to an Object.
     */
    Object parse(String str) throws ParseException;

    /**
     * Returns <code>true</code> if this formatter is thread safe, <code>false</code>
     * otherwise.
     */
    boolean isThreadSafe();
}
