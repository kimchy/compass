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

package org.compass.annotations;

/**
 * Specifies whether and how a meta-data property should have term vectors.
 *
 * @author kimchy
 */
public enum TermVector {
    /**
     * Not applicable. Where possible, will use a global setting for this.
     */
    NA,

    /**
     * Do not store term vectors.
     */
    NO,

    /**
     * Store the term vectors of each document. A term vector is a list of
     * the document's terms and their number of occurences in that document.
     */
    YES,

    /**
     * Store the term vector + token position information
     *
     * @see #YES
     */
    WITH_POSITIONS,

    /**
     * Store the term vector + Token offset information
     *
     * @see #YES
     */
    WITH_OFFSETS,

    /**
     * Store the term vector + Token position and offset information
     *
     * @see #YES
     * @see #WITH_POSITIONS
     * @see #WITH_OFFSETS
     */
    WITH_POSITIONS_OFFSETS
}
