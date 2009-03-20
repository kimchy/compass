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

package org.compass.core.util.reflection.asm;

import org.objectweb.asm.Type;

/**
 * @author kimchy
 */
public class Boxer {

    final static public String INTERNAL_NAME = Type.getInternalName(Boxer.class);

    public static Object box(boolean v) {
        return v;
    }

    public static Object box(byte v) {
        return v;
    }

    public static Object box(char v) {
        return v;
    }

    public static Object box(short v) {
        return v;
    }

    public static Object box(int v) {
        return v;
    }

    public static Object box(long v) {
        return v;
    }

    public static Object box(float v) {
        return v;
    }

    public static Object box(double v) {
        return v;
    }
}
