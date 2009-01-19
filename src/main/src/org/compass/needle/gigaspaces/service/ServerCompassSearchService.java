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

import com.j_spaces.core.client.ReadModifiers;
import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.openspaces.core.GigaSpace;

/**
 * This is the server side implemenation of the search service that should be exposed using OpenSpaces
 * service exporter using sync remoting (as a filter to the Space).
 *
 * <p>Initialized with a Compass instance to perform the search with, and a GigaSpace instnace to load
 * data objects from the space. Note, since objects are loaded from the space, <code>compass.osem.supportUnmarshall</code>
 * in Compass should be set to <code>false</code>.
 *
 * @author kimchy
 */
public class ServerCompassSearchService implements CompassSearchService {

    private CompassTemplate compassTemplate;

    private GigaSpace gigaSpace;

    public void setCompass(Compass compass) {
        this.compassTemplate = new CompassTemplate(compass);
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public SearchResourceResults searchResource(final String query) {
        return searchResource(query, -1);
    }

    public SearchResourceResults searchResource(String query, int maxResults) {
        return searchResource(query, maxResults, -1);
    }

    public SearchResourceResults searchResource(final String query, final int maxResults, final float fromScore) {
        return compassTemplate.execute(new CompassCallback<SearchResourceResults>() {
            public SearchResourceResults doInCompass(CompassSession session) throws CompassException {
                CompassHits hits = session.find(query);

                int startFrom = 0;
                if (fromScore != -1) {
                    while (true) {
                        if (startFrom >= hits.length()) {
                            break;
                        }
                        if (hits.score(startFrom) < fromScore) {
                            break;
                        }
                        startFrom++;
                    }
                }

                int endWith = hits.length();
                if (maxResults != -1) {
                    endWith = startFrom + maxResults;
                    if (endWith > hits.length()) {
                        endWith = hits.length();
                    }
                }
                
                SearchResourceResult[] results = new SearchResourceResult[endWith - startFrom];
                for (int i = startFrom; i < endWith; i++) {
                    results[i - startFrom] = new SearchResourceResult(hits.score(i), hits.resource(i));
                }
                return new SearchResourceResults(results, hits.length());
            }
        });
    }

    public SearchResults search(final String query) {
        return search(query, -1);
    }

    public SearchResults search(String query, int maxResults) {
        return search(query, maxResults, -1);
    }

    public SearchResults search(final String query, final int maxResults, final float fromScore) {
        return compassTemplate.execute(new CompassCallback<SearchResults>() {
            public SearchResults doInCompass(CompassSession session) throws CompassException {
                CompassHits hits = session.find(query);

                int startFrom = 0;
                if (fromScore != -1) {
                    while (true) {
                        if (startFrom >= hits.length()) {
                            break;
                        }
                        if (hits.score(startFrom) < fromScore) {
                            break;
                        }
                        startFrom++;
                    }
                }

                int endWith = hits.length();
                if (maxResults != -1) {
                    endWith = startFrom + maxResults;
                    if (endWith > hits.length()) {
                        endWith = hits.length();
                    }
                }

                SearchResult[] results = new SearchResult[endWith - startFrom];
                for (int i = startFrom; i < endWith; i++) {
                    Object data = gigaSpace.read(hits.data(i), 0, ReadModifiers.MATCH_BY_ID);
                    results[i - startFrom] = new SearchResult(hits.score(i), data);
                }
                return new SearchResults(results, hits.length());
            }
        });
    }
}
