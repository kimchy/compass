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

package org.compass.core.accessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.compass.core.CompassException;
import org.compass.core.util.ClassUtils;

/**
 * Accesses fields directly.
 * <p>
 * Initial version taken from hibernate.
 * </p>
 * 
 * @author kimchy
 */
public class DirectPropertyAccessor implements PropertyAccessor {

    public static final class DirectGetter implements Getter {

        private static final long serialVersionUID = 3257848800692155955L;

        private final transient Field field;

        private final Class clazz;

        private final String name;

        DirectGetter(Field field, Class clazz, String name) {
            this.field = field;
            this.clazz = clazz;
            this.name = name;
        }

        public Object get(Object target) throws CompassException {
            try {
                return field.get(target);
            } catch (Exception e) {
                throw new PropertyAccessException(e, "could not get a field value by reflection", false, clazz, name);
            }
        }

        public String getName() {
            return name;
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public Class getReturnType() {
            return field.getType();
        }

        Object readResolve() {
            return new DirectGetter(getField(clazz, name), clazz, name);
        }

        public String toString() {
            return "DirectGetter(" + clazz.getName() + '.' + name + ')';
        }
    }

    public static final class DirectSetter implements Setter {

        private static final long serialVersionUID = 3832625071100277812L;

        private final transient Field field;

        private final Class clazz;

        private final String name;

        DirectSetter(Field field, Class clazz, String name) {
            this.field = field;
            this.clazz = clazz;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public void set(Object target, Object value) throws CompassException {
            try {
                field.set(target, value);
            } catch (Exception e) {
                throw new PropertyAccessException(e, "could not set a field value by reflection", true, clazz, name);
            }
        }

        public String toString() {
            return "DirectSetter(" + clazz.getName() + '.' + name + ')';
        }

        Object readResolve() {
            return new DirectSetter(getField(clazz, name), clazz, name);
        }
    }

    private static Field getField(Class clazz, String name) throws PropertyNotFoundException {
        if (clazz == null || clazz == Object.class) {
            throw new PropertyNotFoundException("field not found: " + name);
        }
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException nsfe) {
            field = getField(clazz.getSuperclass(), name);
        }
        if (!ClassUtils.isPublic(clazz, field))
            field.setAccessible(true);
        return field;
    }

    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new DirectGetter(getField(theClass, propertyName), theClass, propertyName);
    }

    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new DirectSetter(getField(theClass, propertyName), theClass, propertyName);
    }

}
