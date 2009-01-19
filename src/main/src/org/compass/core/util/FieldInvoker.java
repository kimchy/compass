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

package org.compass.core.util;

import java.lang.reflect.Field;

/**
 *
 * @author kimchy
 */
public class FieldInvoker {

    private Class targetClass;

    private String targetField;

    private Field fieldObject;

    private Object targetObject;

    private Object targetValue;

    public FieldInvoker(Class targetClass, String tagetField) {
        setTargetClass(targetClass);
        setTargetField(tagetField);
    }

    public FieldInvoker prepare() throws NoSuchFieldException {
        Assert.notNull(this.targetClass, "targetClass is required");
        Assert.notNull(this.targetField, "targetMethod is required");

        this.fieldObject = targetClass.getDeclaredField(this.targetField);
        this.fieldObject.setAccessible(true);

        return this;
    }

    /**
     * Set the target class on which to call the target method.
     * Only necessary when the target method is static; else,
     * a target object needs to be specified anyway.
     * @see #setTargetObject
     */
    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Return the target class on which to call the target method.
     */
    public Class getTargetClass() {
        return targetClass;
    }

    /**
     * Set the target object on which to call the target method.
     * Only necessary when the target method is not static;
     * else, a target class is sufficient.
     * @see #setTargetClass
     */
    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
        if (targetObject != null) {
            this.targetClass = targetObject.getClass();
        }
    }

    /**
     * Return the target object on which to call the target method.
     */
    public Object getTargetObject() {
        return targetObject;
    }


    /**
     * Set the name of the field to be invoked.
     * @see #setTargetClass
     * @see #setTargetObject
     */
    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    /**
     * Return the name of the field to be invoked.
     */
    public String getTargetField() {
        return targetField;
    }

    public Object getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the value that will be set to the field
     */
    public void setTargetValue(Object targetValue) {
        this.targetValue = targetValue;
    }


    public Object get() throws IllegalAccessException {
        return get(this.targetObject);
    }

    public Object get(Object targetObject) throws IllegalAccessException {
        Assert.notNull(targetObject, "targetObject is required");
        return this.fieldObject.get(targetObject);
    }

    public void set() throws IllegalAccessException {
        set(this.targetObject, this.targetValue);
    }

    public void set(Object targetObject, Object targetValue) throws IllegalAccessException {
        Assert.notNull(targetObject, "targetObject is required");
        Assert.notNull(targetValue, "targetVaue is required");
        this.fieldObject.set(targetObject, targetValue);
    }
}
