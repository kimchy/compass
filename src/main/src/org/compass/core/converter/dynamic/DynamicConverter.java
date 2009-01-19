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

package org.compass.core.converter.dynamic;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.basic.FormatConverter;

/**
 * A converter that evaluates dynamic expressions using a dynamic
 * expression library or scripting library.
 *
 * @author kimchy
 */
public interface DynamicConverter extends Converter {

    static final String DATA_CONTEXT_KEY = "data";

    /**
     * Copies over the dynamic converter.
     */
    DynamicConverter copy();

    /**
     * Sets the expression this dynamic converter will ecaluate.
     *
     * @param expression The expression to evaluate
     * @throws ConversionException
     */
    void setExpression(String expression) throws ConversionException;

    /**
     * If the dynamic expression evaluates to a formatable object (like Date),
     * the format converter that will be used to format it.
     */
    void setFormatConverter(FormatConverter formatConverter);

    /**
     * The return type of the evaluated expression.
     */
    void setType(Class type);

    /**
     * The return type of the evaluated expression.
     */
    Class getType();
}
