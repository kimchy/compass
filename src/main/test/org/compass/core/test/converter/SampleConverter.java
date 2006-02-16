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

package org.compass.core.test.converter;

import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class SampleConverter extends AbstractBasicConverter {

    private String seperator = "XXX";

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        A.TwoStringsValue value = (A.TwoStringsValue) o;
        return value.getValue1() + seperator + value.getValue2();
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) {
        A.TwoStringsValue value = new A.TwoStringsValue();
        value.setValue1(str.substring(0, str.indexOf(seperator)));
        value.setValue2(str.substring(str.indexOf(seperator) + seperator.length()));
        return value;
    }

    public String getSeperator() {
        return seperator;
    }

    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }
}
