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
 * Wrapper that provides a lazily loaded hit from
 * {@link CompassHitsOperations}.
 *
 * @author kimchy
 */
public interface CompassHit extends Serializable {

    /**
     * Returns the alias value of the hit.
     *
     * @return The alias.
     * @throws CompassException
     */
    String getAlias() throws CompassException;

    /**
     * Same as {@link #getAlias()}.
     */
    String alias() throws CompassException;

    /**
     * Returns the object for this hit.
     *
     * @return The object data of the hit.
     * @throws CompassException
     * @see CompassHits#data(int)
     */
    Object getData() throws CompassException;

    /**
     * Same as {@link #getData()} just shorter.
     */
    Object data() throws CompassException;

    /**
     * Returns the {@link Resource} for this hit.
     *
     * @return The {@link Resource} of the hit.
     * @throws CompassException
     * @see CompassHits#resource(int)
     */
    Resource getResource() throws CompassException;

    /**
     * Same as {@link #getResource()}.
     */
    Resource resource() throws CompassException;

    /**
     * Returns the score for this hit.
     *
     * @return The score of the hit.
     * @throws CompassException
     * @see CompassHits#score(int)
     */
    float getScore() throws CompassException;

    /**
     * Same as {@link #getScore()}.
     */
    float score() throws CompassException;

    /**
     * Returns a cached highlighted text the maps to the hit.
     * <p/>
     * Highlighted text is automatically cached when using {@link CompassHighlighter}
     * using {@link CompassHits#highlighter(int)}.
     *
     * @return The cached highlighted hits
     * @throws CompassException
     */
    CompassHighlightedText getHighlightedText() throws CompassException;

    /**
     * Same as {@link #getHighlightedText()}.
     */
    CompassHighlightedText highlightedText() throws CompassException;
}
