/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.mapping.json;

import org.compass.core.util.Parameter;

/**
 * @author kimchy
 */
public final class NamingType extends Parameter {

    private NamingType(String name) {
        super(name);
    }

    public static final NamingType PLAIN = new NamingType("PLAIN");

    public static final NamingType FULL = new NamingType("FULL");

    public static String toString(NamingType namingType) {
        if (namingType == NamingType.PLAIN) {
            return "plain";
        } else if (namingType == NamingType.FULL) {
            return "full";
        }
        throw new IllegalArgumentException("Can't find naming type for [" + namingType + "]");
    }

    public static NamingType fromString(String namingType) {
        if ("plain".equalsIgnoreCase(namingType)) {
            return NamingType.PLAIN;
        } else if ("full".equalsIgnoreCase(namingType)) {
            return NamingType.FULL;
        }
        throw new IllegalArgumentException("Can't find naming type for [" + namingType + "]");
    }

}
