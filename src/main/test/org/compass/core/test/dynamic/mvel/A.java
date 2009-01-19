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

package org.compass.core.test.dynamic.mvel;

import java.util.Collection;
import java.util.Date;

/**
 * @author kimchy
 */
public class A {

    private Long id;

    private String value;

    private String value2;

    private Date date;

    private String[] valuesArr;

    private Collection valuesCol;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String[] getValuesArr() {
        return valuesArr;
    }

    public void setValuesArr(String[] valuesArr) {
        this.valuesArr = valuesArr;
    }

    public Collection getValuesCol() {
        return valuesCol;
    }

    public void setValuesCol(Collection valuesCol) {
        this.valuesCol = valuesCol;
    }
}