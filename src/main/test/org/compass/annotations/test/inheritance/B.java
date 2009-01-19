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

package org.compass.annotations.test.inheritance;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable(poly = true, alias = "b")
public class B extends A {

    private String value3;

    @SearchableProperty(name = "value1e")
    public String getValue1() {
        return super.getValue1();
    }

    public void setValue1(String value1) {
        super.setValue1(value1);
    }

    @SearchableProperty(name = "value2e", override = false)
    public String getValue2() {
        return super.getValue2();
    }

    public void setValue2(String value2) {
        super.setValue2(value2);
    }

    @SearchableProperty(name = "value3e")
    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }
}
