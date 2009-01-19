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

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassException;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.util.StringUtils;

/**
 * <p>A general Search Controller that perform the search operations. The seardch controller is
 * therad safe.
 *
 * <p>Will perform the search operation on the <code>Compass</code> instance using the query
 * supplied by the {@link org.compass.core.support.search.CompassSearchCommand}.
 *
 * <p>Pagination will be enabled if <code>pageSize</code> property is set on the controller,
 * as well as providing the <code>page</code> number property on the
 * {@link org.compass.core.support.search.CompassSearchCommand}.
 *
 * <p>The search controller provides several extension points, including
 * {@link #buildQuery(CompassSearchCommand,org.compass.core.CompassSession)},
 * {@link #doProcessBeforeDetach(CompassSearchCommand,org.compass.core.CompassSession,org.compass.core.CompassHits,int,int)}
 * and {@link #doProcessAfterDetach(CompassSearchCommand,org.compass.core.CompassSession,org.compass.core.CompassDetachedHits)}.
 *
 * @author kimchy
 */
public class CompassSearchHelper {

    private CompassTemplate compassTemplate;

    private Integer pageSize;

    /**
     * Creates a new compass search helper based on Compass without pagination.
     *
     * @param compass The compass instance to use
     */
    public CompassSearchHelper(Compass compass) {
        this(compass, null);
    }

    /**
     * Creates a new compass search helper based on Compass with pagination.
     *
     * @param compass  The Compass instance
     * @param pageSize The page size
     */
    public CompassSearchHelper(Compass compass, Integer pageSize) {
        this.compassTemplate = new CompassTemplate(compass);
        this.pageSize = pageSize;
    }

    public CompassSearchResults search(final CompassSearchCommand command) throws CompassException {
        if (!StringUtils.hasText(command.getQuery()) && command.getCompassQuery() == null) {
            return new CompassSearchResults(new CompassHit[0], 0, 0);
        }
        return (CompassSearchResults) compassTemplate.execute(new CompassCallback() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return performSearch(command, session);
            }
        });
    }

    public CompassSearchResults searchLocal(final CompassSearchCommand command) throws CompassException {
        if (!StringUtils.hasText(command.getQuery()) && command.getCompassQuery() == null) {
            return new CompassSearchResults(new CompassHit[0], 0, 0);
        }
        return (CompassSearchResults) compassTemplate.executeLocal(new CompassCallback() {
            public Object doInCompass(CompassSession session) throws CompassException {
                return performSearch(command, session);
            }
        });
    }

    /**
     * Performs the actual search operation. If pageSize is set, will perform pagination using the
     * provided size, if not, will return all the hits. Also allows for several extensions points:
     * {@link #buildQuery(CompassSearchCommand,org.compass.core.CompassSession)},
     * {@link #doProcessBeforeDetach(CompassSearchCommand,org.compass.core.CompassSession,org.compass.core.CompassHits,int,int)}
     * and {@link #doProcessAfterDetach(CompassSearchCommand,org.compass.core.CompassSession,org.compass.core.CompassDetachedHits)}.
     *
     * @param searchCommand The search command to perform the search
     * @param session       CompassSession to execute the search with
     * @return search results
     */
    protected CompassSearchResults performSearch(CompassSearchCommand searchCommand, CompassSession session) {
        long time = System.currentTimeMillis();
        CompassQuery query = buildQuery(searchCommand, session);
        CompassHits hits = query.hits();
        CompassDetachedHits detachedHits;
        CompassSearchResults.Page[] pages = null;
        if (pageSize == null) {
            doProcessBeforeDetach(searchCommand, session, hits, -1, -1);
            detachedHits = hits.detach();
            doProcessAfterDetach(searchCommand, session, detachedHits);
        } else {
            int iPageSize = pageSize.intValue();
            int page = 0;
            int hitsLength = hits.getLength();
            if (searchCommand.getPage() != null) {
                page = searchCommand.getPage().intValue();
            }
            int from = page * iPageSize;
            if (from > hits.getLength()) {
                // from can't be negative
                from = Math.max(0, hits.getLength() - iPageSize);
                doProcessBeforeDetach(searchCommand, session, hits, from, (hitsLength - from));
                detachedHits = hits.detach(from, (hitsLength - from));
            } else if ((from + iPageSize) > hitsLength) {
                doProcessBeforeDetach(searchCommand, session, hits, from, (hitsLength - from));
                detachedHits = hits.detach(from, (hitsLength - from));
            } else {
                doProcessBeforeDetach(searchCommand, session, hits, from, iPageSize);
                detachedHits = hits.detach(from, iPageSize);
            }
            doProcessAfterDetach(searchCommand, session, detachedHits);
            int numberOfPages = (int) Math.ceil((float) hitsLength / iPageSize);
            pages = new CompassSearchResults.Page[numberOfPages];
            for (int i = 0; i < pages.length; i++) {
                pages[i] = new CompassSearchResults.Page();
                pages[i].setFrom(i * iPageSize + 1);
                pages[i].setSize(iPageSize);
                pages[i].setTo((i + 1) * iPageSize);
                if (from >= (pages[i].getFrom() - 1) && from < pages[i].getTo()) {
                    pages[i].setSelected(true);
                } else {
                    pages[i].setSelected(false);
                }
            }
            if (numberOfPages > 0) {
                CompassSearchResults.Page lastPage = pages[numberOfPages - 1];
                if (lastPage.getTo() > hitsLength) {
                    lastPage.setSize(hitsLength - lastPage.getFrom() + 1);
                    lastPage.setTo(hitsLength);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        CompassSearchResults searchResults = new CompassSearchResults(detachedHits.getHits(), time, hits.length());
        searchResults.setPages(pages);
        return searchResults;
    }

    /**
     * <p>Acts as an extension point for search controller that wish to build
     * different CompassQueries. Since the search command can hold either a
     * {@link org.compass.core.CompassQuery} or a query string, will first
     * check if the {@link org.compass.core.CompassQuery} is set, and if not,
     * will use the query search string.
     *
     * <p>The default implementation when query string is provided uses the session to create a query
     * builder and use the queryString option, i.e.:
     * <code>session.queryBuilder().queryString(searchCommand.getQuery().trim()).toQuery();</code>.
     *
     * <p>Some other interesting options might be to add sorting to the query,
     * adding other queries using a boolean query, or executing a different
     * query.
     */
    protected CompassQuery buildQuery(CompassSearchCommand searchCommand, CompassSession session) {
        if (searchCommand.getCompassQuery() != null) {
            return searchCommand.getCompassQuery();
        }
        return session.queryBuilder().queryString(searchCommand.getQuery().trim()).toQuery();
    }

    /**
     * An option to perform any type of processing before the hits are detached.
     */
    protected void doProcessBeforeDetach(CompassSearchCommand searchCommand, CompassSession session, CompassHits hits,
                                         int from, int size) {

    }

    /**
     * An option to perform any type of processing after the hits are detached.
     */
    protected void doProcessAfterDetach(CompassSearchCommand searchCommand, CompassSession session,
                                        CompassDetachedHits hits) {

    }

    /**
     * Returns the page size for the pagination of the results. If not set, not
     * pagination will be used.
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size for the pagination of the results. If not set, not
     * pagination will be used.
     *
     * @param pageSize The page size for pagination of the results
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
