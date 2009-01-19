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
import org.compass.annotations.SearchableConstant;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable(poly = true, alias = "a1")
@SearchableConstant(name = "abase", values = {"abasevalue"})
public class A {

    private int id;

    private String value1;

    private String value2;

    @SearchableId
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SearchableProperty(name = "value1")
    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    @SearchableProperty(name = "value2")
    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
