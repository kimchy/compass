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

package org.compass.spring.web.mvc;

import org.compass.core.CompassHit;

/**
 * The results object returned by
 * {@link org.compass.spring.web.mvc.CompassSearchController} when the search
 * operation on <code>Compass</code> is executed.
 * <p>
 * Holds the time it took to perform the search operation (in milliseconds), an
 * array of <code>CompassHit</code> (which might be all the hits, or only
 * paginated hits) and an array of <code>Page</code>s if using the pagination
 * feature..
 * 
 * @author kimchy
 */
public class CompassSearchResults {

    /**
     * A class which holds the page data if using the pagination feature.
     * 
     * @author kimchy
     * 
     */
    public static class Page {
        private int from;

        private int to;

        private int size;

        private boolean selected;

        /**
         * Returns the hit number the page starts from.
         * 
         * @return
         */
        public int getFrom() {
            return from;
        }

        /**
         * Sets the hit number the page starts from.
         * 
         * @param from
         */
        public void setFrom(int from) {
            this.from = from;
        }

        /**
         * Returns <code>true</code> if the page is selected, i.e. the results
         * that are shown are part of the page.
         * 
         * @return
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets if the page is selected or not.
         * 
         * @param selected
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        /**
         * Returns the size of the hits in the page.
         * 
         * @return
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets the size of the hits in the page.
         * 
         * @param size
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         * Returns the hit number that the page ends at.
         * 
         * @return
         */
        public int getTo() {
            return to;
        }

        /**
         * Sets the hit number that the page ends at.
         * 
         * @param to
         */
        public void setTo(int to) {
            this.to = to;
        }
    }

    private CompassHit[] hits;

    private Page[] pages;

    private long searchTime;

    public CompassSearchResults(CompassHit[] hits, long searchTime) {
        this.hits = hits;
        this.searchTime = searchTime;
    }

    /**
     * Returns the hits that resulted from the search operation. Might hold all
     * the hits (not using pagination) or only the hits that belong to the
     * selected page (if using pagination).
     * 
     * @return
     */
    public CompassHit[] getHits() {
        return hits;
    }

    /**
     * Returns the time that it took to perform the search operation (in
     * milliseconds).
     * 
     * @return
     */
    public long getSearchTime() {
        return searchTime;
    }

    /**
     * Returns the pages that construct all the results.
     * 
     * @return
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
