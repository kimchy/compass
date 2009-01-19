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

package org.compass.core.support.search;

import org.compass.core.CompassHit;

/**
 * The results object returned by
 * {@link org.compass.core.support.search.CompassSearchHelper} when the search
 * operation on <code>Compass</code> is executed.
 * <p>
 * Holds the time it took to perform the search operation (in milliseconds), an
 * array of <code>CompassHit</code> (which might be all the hits, or only
 * paginated hits) and an array of <code>Page</code>s if using the pagination
 * feature.
 *
 * @author kimchy
 */
public class CompassSearchResults {

    /**
     * A class which holds the page data if using the pagination feature.
     *
     * @author kimchy
     */
    public static class Page {
        private int from;

        private int to;

        private int size;

        private boolean selected;

        /**
         * Returns the hit number the page starts from.
         */
        public int getFrom() {
            return from;
        }

        /**
         * Sets the hit number the page starts from.
         */
        public void setFrom(int from) {
            this.from = from;
        }

        /**
         * Returns <code>true</code> if the page is selected, i.e. the results
         * that are shown are part of the page.
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets if the page is selected or not.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        /**
         * Returns the size of the hits in the page.
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets the size of the hits in the page.
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         * Returns the hit number that the page ends at.
         */
        public int getTo() {
            return to;
        }

        /**
         * Sets the hit number that the page ends at.
         */
        public void setTo(int to) {
            this.to = to;
        }
    }

    private CompassHit[] hits;

    private Page[] pages;

    private long searchTime;

    private int totalHits;

    public CompassSearchResults(CompassHit[] hits, long searchTime, int totalHits) {
        this.hits = hits;
        this.searchTime = searchTime;
        this.totalHits = totalHits;
    }

    /**
     * Returns the hits that resulted from the search operation. Might hold all
     * the hits (not using pagination) or only the hits that belong to the
     * selected page (if using pagination).
     *
     * @return The hits
     */
    public CompassHit[] getHits() {
        return hits;
    }

    /**
     * Returns the time that it took to perform the search operation (in
     * milliseconds).
     *
     * @return How long it took to perform the serarch in milli-seconds.
     */
    public long getSearchTime() {
        return searchTime;
    }

    /**
     * Returns the total number of hits resulted from this search query.
     *
     * @return The total number of hits
     */
    public int getTotalHits() {
        return this.totalHits;
    }

    /**
     * Returns the pages that construct all the results.
     *
     * @return The pages that holds all the results
     */
    public Page[] getPages() {
        return pages;
    }

    /**
     * Sets the pages that contruct all the results.
     *
     * @param pages
     */
    public void setPages(Page[] pages) {
        this.pages = pages;
    }
}
