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

package org.compass.core.mapping.osem.builder;

/**
 * A convenient enum for popular property accessor such as "field" and "property".
 *
 * @author kimchy
 */
public enum Accessor {
    /**
     * The accessor will use the class field.
     */
    FIELD,
    /**
     * The accessor will use the class proeprty (getter and optionally setter)
     */
    PROPERTY;


    @Override
    public String toString() {
        switch (this) {
            case FIELD:
                return "field";
            case PROPERTY:
                return "property";
            default:
                throw new IllegalStateException("Can't handle type [" + this + "]");
        }
    }
}
