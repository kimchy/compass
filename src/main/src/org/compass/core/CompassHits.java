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
 * Holds hits returned from a search performed by compass. Can be used within a
 * transaction context. For hits to be used outside of a transactional context,
 * the {@link #detach()} and {@link #detach(int, int)} can be used.
 * <p/>
 * Also allows for highlighting using {@link #highlighter(int)}, and any highlighting
 * operation (that returns a single <code>String</code>) will be cached within the
 * hits (and also moved to the detached hits, if {@link #detach(int, int)} is called),
 * and can be used by {@link CompassHitsOperations#highlightedText(int)}.
 *
 * @author kimchy
 */
public interface CompassHits extends CompassHitsOperations {

    /**
     * Detaches a seperate <code>CompassHits</code>, holds all the data. The
     * detached hits preloads all the data, so it can be used outside of a
     * transaction. NOTE: Be carefull when using the method, since it will take
     * LONG time to load a large hits result set.
     *
     * @return A detached hits.
     * @throws CompassException 
     */
    CompassDetachedHits detach() throws CompassException;

    /**
     * Detaches a seperate <code>CompassHits</code>, which starts from the
     * given from parameter, and has the specified size. The detached hits
     * preloads all the data, so it can be used outside of a transaction.
     *
     * @param from The index that the sub hits starts from.
     * @param size The size of the sub hits.
     * @return A detached sub hits.
     * @throws CompassException
     */
    CompassDetachedHits detach(int from, int size) throws CompassException, IllegalArgumentException;

    /**
     * Returns the highlighter that maps the n'th hit.
     * <p/>
     * Note, that any highlighting operation (that returns a single <code>String</code>)
     * will be cached within the hits (and also moved to the detached hits, if
     * {@link #detach(int, int)} is called), and can be used by
     * {@link CompassHitsOperations#highlightedText(int)}.
     *
     * @param n The n'th hit.
     * @return The highlighter.
     * @throws CompassException
     */
    CompassHighlighter highlighter(int n) throws CompassException;

    /**
     * Closes the hits object. Note that it is an optional operation since it
     * will be closed transperantly when the transaction is closed.
     * <p/>
     * It is provided for more controlled resource management
     *
     * @throws CompassException
     */
    void close() throws CompassException;
}
