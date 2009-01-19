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

package org.compass.core.util.reflection.plain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.compass.core.util.reflection.ReflectionConstructor;

/**
 * A plain implemenation of {@link org.compass.core.util.reflection.ReflectionConstructor}
 * that simply delegates operations to {@link java.lang.reflect.Constructor}.
 *
 * @author kimchy
 */
public class PlainReflectionConstructor implements ReflectionConstructor {

    private Constructor constructor;

    public PlainReflectionConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public Object newInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructor.newInstance();
    }
}
