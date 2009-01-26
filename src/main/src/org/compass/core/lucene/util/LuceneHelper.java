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

package org.compass.core.lucene.util;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilter;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.impl.DefaultCompassHits;
import org.compass.core.impl.DefaultCompassQuery;
import org.compass.core.impl.DefaultCompassQueryFilter;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassQuery;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalResource;

/**
 * Allows to create Compass related objects based on external (internally no supported by Compass)
 * Lucene objects.
 *
 * @author kimchy
 */
public abstract class LuceneHelper {

    /**
     * Creates a new {@link CompassQuery} based on a Lucene {@link Query}.
     *
     * <p>Allows to create {@link CompassQuery} based on external Lucene {@link Query} that is not supported
     * by one of Compass query builders.
     *
     * @param compass Compass instance
     * @param query   The lucene query to wrap
     * @return A compass query wrapping the lucene query
     */
    public static CompassQuery createCompassQuery(Compass compass, Query query) {
        InternalCompass internalCompass = (InternalCompass) compass;
        SearchEngineQuery searchEngineQuery =
                new LuceneSearchEngineQuery((LuceneSearchEngineFactory) internalCompass.getSearchEngineFactory(), query);
        return new DefaultCompassQuery(searchEngineQuery, internalCompass);
    }

    /**
     * Creates a new {@link CompassQuery} based on a Lucene {@link Query}.
     *
     * <p>Allows to create {@link CompassQuery} based on external Lucene {@link Query} that is not supported
     * by one of Compass query builders.
     *
     * @param session Compass session
     * @param query   The lucene query to wrap
     * @return A compass query wrapping the lucene query
     */
    public static CompassQuery createCompassQuery(CompassSession session, Query query) {
        InternalCompassSession internalCompassSession = (InternalCompassSession) session;
        SearchEngineQuery searchEngineQuery =
                new LuceneSearchEngineQuery((LuceneSearchEngineFactory) internalCompassSession.getCompass().getSearchEngineFactory(), query);
        InternalCompassQuery compassQuery = new DefaultCompassQuery(searchEngineQuery, internalCompassSession.getCompass());
        compassQuery.attach(session);
        return compassQuery;
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineQuery} of the given {@link CompassQuery}.
     * <p/>
     * Can be used for example to add custom Sorting using
     * {@link LuceneSearchEngineQuery#addSort(org.apache.lucene.search.SortField)}, or get the actual lucene query
     * using {@link org.compass.core.lucene.engine.LuceneSearchEngineQuery#getQuery()}.
     *
     * @param query The compass query to extract the lucene search engine query from
     * @return The lucene search engine query extracted from the compass query
     */
    public static LuceneSearchEngineQuery getLuceneSearchEngineQuery(CompassQuery query) {
        return (LuceneSearchEngineQuery) ((DefaultCompassQuery) query).getSearchEngineQuery();
    }

    /**
     * Creates a new {@link CompassQueryFilter} based on a Lucene {@link Filter}.
     * <p/>
     * Allows to create {@link CompassQueryFilter} based on external Lucene {@link Filter} that is not supported
     * by one fo Comapss query filter builders.
     *
     * @param session Comapss session
     * @param filter  The lucene filter to wrap
     * @return A compass query filter wrapping lucene query.
     */
    public static CompassQueryFilter createCompassQueryFilter(CompassSession session, Filter filter) {
        SearchEngineQueryFilter searchEngineQueryFilter = new LuceneSearchEngineQueryFilter(filter);
        return new DefaultCompassQueryFilter(searchEngineQueryFilter);
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineQueryFilter} of the given {@link CompassQueryFilter}.
     * <p/>
     * Can be used to get the actual Lucene {@link Filter} using
     * {@link org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter#getFilter()}.
     *
     * @param filter The compass query filter to extract the lucene search engine query filter from
     * @return The lucene search engine query filter extracted from the compass query filter
     */
    public static LuceneSearchEngineQueryFilter getLuceneSearchEngineQueryFilter(CompassQueryFilter filter) {
        return (LuceneSearchEngineQueryFilter) ((DefaultCompassQueryFilter) filter).getFilter();
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineHits} of the given {@link CompassHits}.
     * <p/>
     * Used mainly to access the actual Lucene {@link org.apache.lucene.search.Hits}, or get
     * Lucene {@link org.apache.lucene.search.Explanation}.
     */
    public static LuceneSearchEngineHits getLuceneSearchEngineHits(CompassHits hits) {
        return (LuceneSearchEngineHits) ((DefaultCompassHits) hits).getSearchEngineHits();
    }

    /**
     * Returns Compass own internal <code>LuceneAnalyzerManager</code>. Can be used
     * to access Lucene {@link org.apache.lucene.analysis.Analyzer} at runtime.
     */
    public static LuceneAnalyzerManager getLuceneAnalyzerManager(Compass compass) {
        return ((LuceneSearchEngineFactory) ((InternalCompass) compass).getSearchEngineFactory()).getAnalyzerManager();
    }

    /**
     * Returns the given search engine "internals" used for search. For Lucene, returns
     * {@link LuceneSearchEngineInternalSearch} which allows to access Lucene
     * {@link org.apache.lucene.index.IndexReader} and {@link org.apache.lucene.search.Searcher}.
     * <p/>
     * The search intenrals will be ones that are executed against the whole index. In order to search on
     * specific aliases or sub indexes, please use {@link #getLuceneInternalSearch(org.compass.core.CompassSession,String[],String[])} .
     *
     * @param session A compass session within a transaction
     * @return Lucene search "internals"
     */
    public static LuceneSearchEngineInternalSearch getLuceneInternalSearch(CompassSession session) {
        return (LuceneSearchEngineInternalSearch) ((InternalCompassSession) session).getSearchEngine().internalSearch(null, null);
    }

    /**
     * Returns the given search engine "internals" used for search. For Lucene, returns
     * {@link LuceneSearchEngineInternalSearch} which allows to access Lucene
     * {@link org.apache.lucene.index.IndexReader} and {@link org.apache.lucene.search.Searcher}.
     * <p/>
     * The search can be narrowed down to specific sub indexes or aliases. A <code>null</code> value
     * means all the sub indexes/aliases.
     *
     * @param session    A compass sessino within a transaction
     * @param subIndexes A set of sub indexes to narrow down the index scope
     * @param aliases    A set of aliases to narrow down the index scope
     * @return Lucene search "internals"
     */
    public static LuceneSearchEngineInternalSearch getLuceneInternalSearch(CompassSession session, String[] subIndexes, String[] aliases) {
        return (LuceneSearchEngineInternalSearch) ((InternalCompassSession) session).getSearchEngine().internalSearch(subIndexes, aliases);
    }

    /**
     * Returns the actual Lucene {@link Document} that the {@link Resource} wraps.
     *
     * @param resource The resource to get the document from
     * @return The Lucene document that resource wraps
     */
    public static Document getDocument(Resource resource) {
        return ((LuceneResource) resource).getDocument();
    }

    /**
     * Returns Lucene {@link TermFreqVector} using the given Compass session and {@link Resource}.
     *
     * @param session  Compass session
     * @param resource The resource to get the term freq vector for
     * @return The term infos freq vector for the given resource
     * @throws SearchEngineException
     */
    public static TermFreqVector[] getTermFreqVectors(CompassSession session, Resource resource)
            throws SearchEngineException {
        resource = ((InternalCompassSession) session).getResourceByIdResourceNoCache(resource);
        String subIndex = ((InternalResource) resource).getSubIndex();
        LuceneSearchEngineInternalSearch internalSearch = getLuceneInternalSearch(session, new String[]{subIndex}, null);
        try {
            return internalSearch.getReader().getTermFreqVectors(((LuceneResource) resource).getDocNum());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to fetch term info for resource [" + resource + "]", e);
        }
    }

    /**
     * Returns Lucene {@link TermFreqVector} for the given property and resource, using the session.
     *
     * @param session      Compass session
     * @param resource     The resource to get the term freq vector for
     * @param propertyName Theh property name (Lucene field name) to get the term freq vector for
     * @return Teh term info freq vector for the given resource and property
     * @throws SearchEngineException
     */
    public static TermFreqVector getTermFreqVector(CompassSession session, Resource resource, String propertyName)
            throws SearchEngineException {
        resource = ((InternalCompassSession) session).getResourceByIdResourceNoCache(resource);
        String subIndex = ((InternalResource) resource).getSubIndex();
        LuceneSearchEngineInternalSearch internalSearch = getLuceneInternalSearch(session, new String[]{subIndex}, null);
        try {
            return internalSearch.getReader().getTermFreqVector(((LuceneResource) resource).getDocNum(), propertyName);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to fetch term info for resource [" + resource + "]", e);
        }
    }

    /**
     * Returns the lucene {@link org.apache.lucene.store.Directory} associated with the given sub index.
     */
    public static Directory getDirectory(Compass compass, String subIndex) {
        return ((LuceneSearchEngineIndexManager) ((InternalCompass) compass).getSearchEngineIndexManager()).getStore().openDirectory(subIndex);
    }

    /**
     * Returns all the values of for the given propery name.
     */
    public static String[] findPropertyValues(CompassSession session, String propertyName) throws SearchEngineException {
        LuceneSearchEngineInternalSearch internalSearch = getLuceneInternalSearch(session);
        ArrayList<String> list = new ArrayList<String>();
        try {
            TermEnum te = internalSearch.getReader().terms(new Term(propertyName, ""));
            while (propertyName.equals(te.term().field())) {
                String value = te.term().text();
                list.add(value);
                if (!te.next()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read property values for property [" + propertyName + "]");
        }
        return list.toArray(new String[list.size()]);
    }
}
