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

package org.compass.core.test.sort;


public class A {

    private Long id;
    
    private String value;

    private String value2;
    
    private int intValue;
    
    private float floatValue;

    private B b;
    
    private A() {
        
    }
    
    public A(Long id, String value, int intValue, float floatValue, B b) {
        this.id = id;
        this.intValue = intValue;
        this.floatValue = floatValue;
        this.value = value;
        this.value2 = value;
        this.b = b;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
