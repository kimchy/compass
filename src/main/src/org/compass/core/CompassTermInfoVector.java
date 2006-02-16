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

/**
 * Holds the term information vector data per property.
 * 
 * @author kimchy
 */
public interface CompassTermInfoVector {

    public static interface OffsetInfo {
        int getEndOffset();

        int getStartOffset();
    }

    /**
     * @return The property this vector is associated with.
     */
    public String getProperty();

    /**
     * @return The number of terms in the term vector.
     */
    public int size();

    /**
     * @return An Array of term texts in ascending order.
     */
    public String[] getTerms();

    /**
     * Array of term frequencies. Locations of the array correspond one to one
     * to the terms in the array obtained from <code>getTerms</code> method.
     * Each location in the array contains the number of times this term occurs
     * in the document or the document field.
     */
    public int[] getTermFrequencies();

    /**
     * Return an index in the term numbers array returned from
     * <code>getTerms</code> at which the term with the specified
     * <code>term</code> appears. If this term does not appear in the array,
     * return -1.
     */
    public int indexOf(String term);

    /**
     * Just like <code>indexOf(int)</code> but searches for a number of terms
     * at the same time. Returns an array that has the same size as the number
     * of terms searched for, each slot containing the result of searching for
     * that term number.
     * 
     * @param terms
     *            array containing terms to look for
     * @param start
     *            index in the array where the list of terms starts
     * @param len
     *            the number of terms in the list
     */
    public int[] indexesOf(String[] terms, int start, int len);

    /**
     * Returns an array of positions in which the term is found. Terms are
     * identified by the index at which its number appears in the term String
     * array obtained from the <code>indexOf</code> method. May return null if
     * positions have not been stored.
     */
    public int[] getTermPositions(int index);

    /**
     * Returns an array of OffsetInfo in which the term is found. May return
     * null if offsets have not been stored.
     * 
     * @see org.apache.lucene.analysis.Token
     * @param index
     *            The position in the array to get the offsets from
     * @return An array of TermVectorOffsetInfo objects or the empty list
     */
    public OffsetInfo[] getOffsets(int index);
}
