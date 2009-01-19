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

package org.compass.needle.gigaspaces.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Holds a list of all the search results.
 *
 * @author kimchy
 */
public class SearchResults implements Externalizable {

    private SearchResult[] results;

    private long totalLength;

    public SearchResults() {
    }

    public SearchResults(SearchResult[] results, long totalLength) {
        this.results = results;
        this.totalLength = totalLength;
    }

    /**
     * Returns the array of the search results.
     */
    public SearchResult[] getResults() {
        return results;
    }

    /**
     * Returns the total length of the hits. Note, this is not the length of the array returned,
     * but the total length of all hits found for the query.
     */
    public long getTotalLength() {
        return totalLength;
    }

    /**
     * Returns the highest score amond the returned hits. This simply returns the first
     * result score if there is one.
     */
    public float getHighestScore() {
        if (results.length > 0) {
            return results[0].getScore();
        }
        return -1;
    }

    /**
     * Returns the lowest score amond the returned hits. This simply returns the last
     * result score if there is one.
     */
    public float getLowestScore() {
        if (results.length > 0) {
            return results[results.length - 1].getScore();
        }
        return -1;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(results.length);
        for (int i = 0; i < results.length; i++) {
            out.writeObject(results[i]);
        }

        out.writeLong(totalLength);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int resultCount = in.readInt();
        results = new SearchResult[resultCount];
        for (int i = 0; i < resultCount; i++) {
            results[i] = (SearchResult) in.readObject();
        }

        totalLength = in.readLong();
    }
}
