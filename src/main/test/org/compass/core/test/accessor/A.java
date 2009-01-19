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

package org.compass.core.test.accessor;

/**
 * @author kimchy
 */
public class A {

    private Long id;

    private String propertyValue;

    public String protectedPropertyValue;

    public String privatePropertyValue;

    public String packagePropertyValue;

    private String fieldValue;

    protected String protectedFieldValue;

    public String publicFieldValue;

    String packageFieldValue;
    
    public A() {
        getPrivatePropertyValue();
        setPrivatePropertyValue(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String checkFieldValue() {
        return fieldValue;
    }

    public void updateFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String checkPublicFieldValue() {
        return publicFieldValue;
    }

    public void updatePublicFieldValue(String fieldValue) {
        this.publicFieldValue = fieldValue;
    }

    public String checkProtectedFieldValue() {
        return protectedFieldValue;
    }

    public void updateProtectedFieldValue(String fieldValue) {
        this.protectedFieldValue = fieldValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    private String getPrivatePropertyValue() {
        return privatePropertyValue;
    }

    private void setPrivatePropertyValue(String privatePropertyValue) {
        this.privatePropertyValue = privatePropertyValue;
    }

    protected String getProtectedPropertyValue() {
        return protectedPropertyValue;
    }

    protected void setProtectedPropertyValue(String protectedPropertyValue) {
        this.protectedPropertyValue = protectedPropertyValue;
    }

    String getPackagePropertyValue() {
        return packagePropertyValue;
    }

    void setPackagePropertyValue(String packagePropertyValue) {
        this.packagePropertyValue = packagePropertyValue;
    }

}
