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

package org.compass.core.spi;

import org.compass.core.Resource;

/**
 * A multiple resource implementation. Exposes {@link Resource} operations as well, which
 * are performed against the current {@link Resource}.
 *
 * @author kimchy
 */
public interface MultiResource extends InternalResource {

    /**
     * Returns the number of {@link Resource}s.
     */
    int size();

    /**
     * Adds another {@link Resource}, and moves the current resource to the newly created one.
     */
    void addResource();

    /**
     * Returns the curren {@link Resource}.
     */
    Resource currentResource();

    /**
     * Returns the {@link Resource} at the given postion.
     */
    Resource resource(int i);

    /**
     * Clears all the saves resources.
     */
    void clear();
}
