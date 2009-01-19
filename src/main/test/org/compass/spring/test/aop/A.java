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

package org.compass.spring.test.aop;

/**
 * @author kimchy
 */
public class A {

    private Long id;

    private String value1;

    private String value2;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the value1.
     */
    public String getValue1() {
        return value1;
    }

    /**
     * @param value1
     *            The value1 to set.
     */
    public void setValue1(String value1) {
        this.value1 = value1;
    }

    /**
     * @return Returns the value2.
     */
    public String getValue2() {
        return value2;
    }

    /**
     * @param value2
     *            The value2 to set.
     */
    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
