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

package org.compass.core.engine.naming;

/**
 * A static path construction, which holds a string representation of the
 * actual path.
 * <p/>
 * Benefits of using this implementation is its fast usage during marshalling/
 * unmarshalling operations. Downside is its memory footprint.
 *
 * @author kimchy
 * @see StaticPropertyNamingStrategy
 */
public class StaticPropertyPath implements PropertyPath {

    private String path;

    public StaticPropertyPath(String path) {
        this.path = path.intern();
    }

    public StaticPropertyPath(PropertyPath root, String name) {
        this.path = root.getPath() + '/' + name;
    }

    public String getPath() {
        return path;
    }

    public PropertyPath hintStatic() {
        return this;
    }

    public int hashCode() {
        return path.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if ((null == object) || (!(object instanceof PropertyPath))) {
            return false;
        }
        return path.equals(((PropertyPath) object).getPath());
    }
}
