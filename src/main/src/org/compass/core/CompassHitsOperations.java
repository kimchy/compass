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

import java.io.Serializable;

/**
 * Mutual operations for hits, for both detached and transactional hits.
 *
 * @author kimchy
 */
public interface CompassHitsOperations extends Serializable, Iterable<CompassHit> {

    /**
     * Returns the number of hits,
     *
     * @return The number of hits.
     */
    int getLength();

    /**
     * Returns the number of hits,
     *
     * @return The number of hits.
     */
    int length();

    /**
     * Returns the object that maps the n'th hit
     *
     * @param n The n'th hit.
     * @return The object.
     * @throws CompassException
     */
    Object data(int n) throws CompassException;

    /**
     * Returns the resource that maps to the n'th hit
     *
     * @param n The n'th hit.
     * @return The resource.
     * @throws CompassException
     */
    Resource resource(int n) throws CompassException;

    /**
     * Returns a compass hit wrapper that maps to the n'th hit
     *
     * @param n The n'th hit.
     * @return The hit.
     * @throws CompassException
     */
    CompassHit hit(int n) throws CompassException;

    /**
     * Returns a cached highlighted text the maps to the n'th hit.
     * <p/>
     * Highlighted text is automatically cached when using {@link CompassHighlighter}
     * using {@link CompassHits#highlighter(int)}.
     *
     * @param n The n'th hit
     * @return A highlighted text cache associated witht the n'th hit
     * @throws CompassException
     */
    CompassHighlightedText highlightedText(int n) throws CompassException;

    /**
     * Returns the score of the n'th hit. Can be a value between 0 and 1,
     * normalised by the highest scoring hit.
     *
     * @param n The n'th hit.
     * @return The score.
     * @throws CompassException
     */
    float score(int n) throws CompassException;

    /**
     * Retrurn the query that resulted in this search hits.
     */
    CompassQuery getQuery();

    /**
     * Returns a suggested query (based on spell check).
     *
     * @see CompassQuery#getSuggestedQuery() 
     */
    CompassQuery getSuggestedQuery();
}
