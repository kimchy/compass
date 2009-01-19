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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.ExecutorRemotingProxyConfigurer;
import org.openspaces.remoting.RemoteResultReducer;
import org.openspaces.remoting.SpaceRemotingInvocation;
import org.openspaces.remoting.SpaceRemotingResult;

/**
 * A client side implementation of the compass search service. Uses sync remoting proxy
 * {@link org.openspaces.remoting.SyncRemotingProxyConfigurer} in order to build a sync remoting
 * proxy around {@link org.compass.needle.gigaspaces.service.CompassSearchService} that perfoms
 * broadcast operations in order to perform the actual search on all the partitions.
 *
 * <p>Has a specific reducer for the broadcast remote invocation that reorders and accumalates the
 * results on the client side.
 *
 * @author kimchy
 */
public class ClientCompassSearchService implements CompassSearchService {

    private CompassSearchService searchService;

    public ClientCompassSearchService(GigaSpace gigaSpace) {
        this.searchService = (CompassSearchService) new ExecutorRemotingProxyConfigurer(gigaSpace, CompassSearchService.class)
                .broadcast(new SearchReducer()).proxy();
    }

    public SearchResourceResults searchResource(String query) {
        return searchService.searchResource(query);
    }

    public SearchResults search(String query) {
        return searchService.search(query);
    }

    public SearchResourceResults searchResource(String query, int maxResults) {
        return searchService.searchResource(query, maxResults);
    }

    public SearchResults search(String query, int maxResults) {
        return searchService.search(query, maxResults);
    }

    public SearchResourceResults searchResource(String query, int maxResults, float fromScore) {
        return searchService.searchResource(query, maxResults, fromScore);
    }

    public SearchResults search(String query, int maxResults, float fromScore) {
        return searchService.search(query, maxResults, fromScore);
    }

    private class SearchReducer implements RemoteResultReducer {

        public Object reduce(SpaceRemotingResult[] spaceRemotingResults, SpaceRemotingInvocation spaceRemotingInvocation) throws Exception {
            for (SpaceRemotingResult result : spaceRemotingResults) {
                if (result.getException() != null) {
                    throw (Exception) result.getException();
                }
            }
            if (spaceRemotingInvocation.getMethodName().equals("searchResource")) {
                ArrayList<SearchResourceResult> ret = new ArrayList<SearchResourceResult>();
                long totalLength = 0;
                for (SpaceRemotingResult result : spaceRemotingResults) {
                    SearchResourceResults searchResourceResults = (SearchResourceResults) result.getResult();
                    for (int i = 0; i < searchResourceResults.getResults().length; i++) {
                        ret.add(searchResourceResults.getResults()[i]);
                    }
                    totalLength += searchResourceResults.getTotalLength();
                }
                SearchResourceResult[] retArray = ret.toArray(new SearchResourceResult[ret.size()]);
                Arrays.sort(retArray, new SearchResourceResultComparator());
                return new SearchResourceResults(retArray, totalLength);
            }
            if (spaceRemotingInvocation.getMethodName().equals("search")) {
                ArrayList<SearchResult> ret = new ArrayList<SearchResult>();
                int totalLength = 0;
                for (SpaceRemotingResult result : spaceRemotingResults) {
                    SearchResults searchResults = (SearchResults) result.getResult();
                    for (int i = 0; i < searchResults.getResults().length; i++) {
                        ret.add(searchResults.getResults()[i]);
                    }
                    totalLength += searchResults.getTotalLength();
                }
                SearchResult[] retArray = ret.toArray(new SearchResult[ret.size()]);
                Arrays.sort(retArray, new SearchResultComparator());
                return new SearchResults(retArray, totalLength);
            }
            return null;
        }
    }

    private class SearchResultComparator implements Comparator<SearchResult> {

        public int compare(SearchResult o1, SearchResult o2) {
            int ret = Float.compare(o1.getScore(), o2.getScore());
            if (ret == -1) {
                return 1;
            }
            if (ret == 1) {
                return -1;
            }
            return ret;
        }
    }

    private class SearchResourceResultComparator implements Comparator<SearchResourceResult> {

        public int compare(SearchResourceResult o1, SearchResourceResult o2) {
            int ret = Float.compare(o1.getScore(), o2.getScore());
            if (ret == -1) {
                return 1;
            }
            if (ret == 1) {
                return -1;
            }
            return ret;
        }
    }
}
