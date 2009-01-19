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

package org.compass.core;

/**
 * Callback interface for Compass code. To be used with CompassTemplate's
 * execute methods, assumably often as anonymous classes within a method
 * implementation. The typical implementation will call
 * CompassSession.load/find/save to perform some operations on searchable
 * objects.
 *
 * @author kimchy
 */
public interface CompassCallback<T> {

    /**
     * Gets called by <code>CompassTemplate.execute</code> with an active
     * Compass Session. Does not need to care about activating or closing the
     * Session, or handling transactions.
     *
     * <p>If called within a thread-bound Compass transaction (initiated by an
     * outer compass transaction abstraction), the code will simply get executed
     * on the outer compass transaction with its transactional semantics.
     *
     * <p>Allows for returning a result object created within the callback, i.e. a
     * domain object or a hits of domain objects. Note that there's special
     * support for single step actions: see CompassTemplate.find etc. A thrown
     * RuntimeException is treated as application exception, it gets propagated
     * to the caller of the template.
     */
    T doInCompass(CompassSession session) throws CompassException;
}
