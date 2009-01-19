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
 * Simple convenience class for the CompassCallback implementation. Allows
 * implementing a doInCompass version without a result, i.e without the need for
 * a return statement.
 * 
 * @author kimchy
 */
public abstract class CompassCallbackWithoutResult implements CompassCallback<Object> {

    public Object doInCompass(CompassSession session) throws CompassException {
        doInCompassWithoutResult(session);
        return null;
    }

    /**
     * Gets called by <code>CompassTemplate.execute</code> with an active
     * Compass Session. Does not need to care about activating or closing the
     * Session, or handling transactions.
     *
     * <p>If called without a thread-bound Compass transaction (initiated by an
     * outer compass transaction abstraction), the code will simply get executed
     * on the outer compass transaction with its transactional semantics.
     * 
     * @param session
     * @throws CompassException
     */
    protected abstract void doInCompassWithoutResult(CompassSession session) throws CompassException;
}
