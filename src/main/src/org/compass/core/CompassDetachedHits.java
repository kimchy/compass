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
 * Holds hits returned from a search performed by compass. Can be used outside of
 * a transaction context. In order to get detached hits, use {@link org.compass.core.CompassHits#detach()}
 * or {@link CompassHits#detach(int, int)}.
 *
 * @author kimchy
 */
public interface CompassDetachedHits extends CompassHitsOperations {

    /**
     * Returns the total number of hits (not just the detached ones).
     *
     * @return The total number of hits.
     */
    int getTotalLength();

    /**
     * Returns the total number of hits (not just the detached ones).
     *
     * @return The total number of hits.
     */
    int totalLength();

    /**
     * Returns all the <code>Resource</code>s as an array.
     * 
     * @return All the <code>Resource</code>s as an array
     */
    Resource[] getResources() throws CompassException;

    /**
     * Returns all the <code>Object</code>s data as an array.
     * 
     * @return An array of all the hits as objects.
     */
    Object[] getDatas() throws CompassException;

    /**
     * Returns all the <code>CompassHit</code>s data as an array. 
     * 
     * @return An array of all the hits.
     * @see CompassHit
     */
    CompassHit[] getHits() throws CompassException;

}
