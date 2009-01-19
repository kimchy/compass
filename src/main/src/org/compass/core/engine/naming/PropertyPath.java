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
 * A path representation abstraction. Note, implementations should
 * take care and override <code>equals</code> and <code>hashCode</code>.
 *
 * @author kimchy
 * @see DynamicPropertyNamingStrategy
 * @see StaticPropertyNamingStrategy
 */
public interface PropertyPath {

    /**
     * Returns the path. Note, the dynamic path construction might occur.
     */
    String getPath();

    /**
     * Compass path construction process will hint on heavily used path
     * elements that might be better off made static. The implementation
     * might decide to create a {@link PropertyPath} implementation that
     * is more static in nature.
     */
    PropertyPath hintStatic();
}
