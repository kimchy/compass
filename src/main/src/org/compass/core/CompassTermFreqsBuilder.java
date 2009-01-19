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
 * <p>A term and frequencies builder, allows to set different settings regarding
 * the available terms for certain properties and their respective frequencies.
 *
 * <p>Allows to narrow down the sub indexes searched either by setting the aliases
 * or the sub indexes (or both of them).
 *
 * <p>Allows to set how the returned terns will be sort. Either by the TERM value or
 * the TERM frequency. Note, the (adjustable) size of terms will always be based on
 * the ones with the highest frequency. Within this size based list (controlled using
 * {@link #setSize(int)}), the sorting will occur.
 *
 * <p>By default, the frequency will be returned according to the search engine
 * implementation values (in Lucene, it is the doc freq). The builder allows to
 * normalize this values. For example, to values between 0 and 1, 0 and 100, or
 * any other range.
 *
 * @author kimchy
 */
public interface CompassTermFreqsBuilder {

    public static enum Sort {

        /**
         * Sort the terms based on their name
         */
        TERM,

        /**
         * Sort the terms based on their frequency (the default)
         */
        FREQ
    }

    /**
     * Sets the size of the results that will be returned. The size will always
     * contain the highest frequencies. Defaults to <code>10</code>.
     */
    CompassTermFreqsBuilder setSize(int size);

    /**
     * Narrow down the terms to specific aliases (which in trun automatically map
     * to a sub index).
     */
    CompassTermFreqsBuilder setAliases(String ... aliases);

    /**
     * Narrow down teh terms to specific classes (which map to aliases).
     */
    CompassTermFreqsBuilder setTypes(Class ... types);

    /**
     * Narrow down the terms to specific sub indexes.
     */
    CompassTermFreqsBuilder setSubIndexes(String ... subIndexes);

    /**
     * Sets the sorting direction of the size based results terms. Note, this is the
     * sorting that will be perfomed on the terms of the highest frequencies based on
     * the {@link #setSize(int)} setting.
     */
    CompassTermFreqsBuilder setSort(Sort sort);

    /**
     * Normalizes the result frequencies based on the provided min and max values. For
     * example, will normalize using 0 to 1 with the lowest frequency mapped to 0, the
     * highest frequency mapped to 1, and the rest are distributed within.
     */
    CompassTermFreqsBuilder normalize(int min, int max);

    /**
     * Builds and returns the term and their frequencies.
     */
    CompassTermFreq[] toTermFreqs() throws CompassException;
}
