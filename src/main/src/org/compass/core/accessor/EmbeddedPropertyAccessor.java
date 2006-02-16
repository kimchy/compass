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

import java.lang.reflect.Method;

import org.compass.core.CompassException;

/**
 * <p>
 * Initial version taken from hibernate.
 * </p>
 * 
 * @author kimchy
 */

public class EmbeddedPropertyAccessor implements PropertyAccessor {

    public static final class EmbeddedGetter implements Getter {

        private static final long serialVersionUID = 3256439201015936821L;

        private final Class clazz;

        EmbeddedGetter(Class clazz) {
            this.clazz = clazz;
        }

        public Object get(Object target) throws CompassException {
            return target;
        }

        public String getName() {
            return null;
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public Class getReturnType() {
            return clazz;
        }

        public String toString() {
            return "EmbeddedGetter(" + clazz.getName() + ')';
        }
    }

    public static final class EmbeddedSetter implements Setter {

        private static final long serialVersionUID = 3835157242151514162L;

        private final Class clazz;

        EmbeddedSetter(Class clazz) {
            this.clazz = clazz;
        }

        public String getName() {
            return null;
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public void set(Object target, Object value) throws CompassException {
        }

        public String toString() {
            return "EmbeddedSetter(" + clazz.getName() + ')';
        }

    }

    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new EmbeddedGetter(theClass);
    }

    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new EmbeddedSetter(theClass);
    }
}
