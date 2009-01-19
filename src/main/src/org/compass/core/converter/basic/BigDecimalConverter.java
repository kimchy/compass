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

package org.compass.core.converter.basic;

import java.math.BigDecimal;

import org.compass.core.converter.basic.format.Formatter;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class BigDecimalConverter extends AbstractNumberConverter<BigDecimal> {

    protected BigDecimal defaultFromString(String str, ResourcePropertyMapping resourcePropertyMapping) {
        return new BigDecimal(str);
    }

    protected BigDecimal fromNumber(Number number) {
        // TODO need to think more about how to do it, we loose data
        return new BigDecimal(number.doubleValue());        
    }

    protected Formatter createSortableFormatter() {
        return null;
    }
}
