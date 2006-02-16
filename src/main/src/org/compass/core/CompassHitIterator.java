/*
 * Copyright 2004-2006 the original author or authors.
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

import java.util.Iterator;

/**
 * An iterator over {@link org.compass.core.CompassDetachedHits} that provides lazy
 * fetching of each resource. {@link CompassDetachedHits#iterator()}
 * returns an instance of this class. Calls to {@link #next()} or {@link #nextHit()} returns a
 * {@link org.compass.core.CompassHit} instance.
 * 
 * @author kimchy
 */

public interface CompassHitIterator extends Iterator {

    /**
     * Returns a {@link CompassHit} instance representing the next hit in
     * {@link CompassHits}.
     * 
     * @return Next {@link CompassHit}.
     */
    CompassHit nextHit();
}
