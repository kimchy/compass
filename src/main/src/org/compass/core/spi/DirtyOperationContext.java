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

import org.compass.core.util.IdentityHashSet;

/**
 * Context object for dirty (create/save/delete) operations.
 *
 * @author kimchy
 */
public class DirtyOperationContext {

    private IdentityHashSet<Object> operatedObjects = new IdentityHashSet<Object>();

    /**
     * Adds the object as one that a dirty operation has been performed on. Note, the
     * identity of the object is used.
     */
    public void addOperatedObjects(Object obj) {
        operatedObjects.add(obj);
    }

    /**
     * Returns <code>true</code> if a dirty operation has been perfomed on the object (based
     * on the object identity). <code>false</code> otherwise.
     */
    public boolean alreadyPerformedOperation(Object obj) {
        return operatedObjects.contains(obj);
    }
}
