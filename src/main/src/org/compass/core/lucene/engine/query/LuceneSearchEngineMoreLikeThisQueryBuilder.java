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

package org.compass.core.lucene.engine.query;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.support.ResourceHelper;

/**
 * @author kimchy
 */
public class LuceneSearchEngineMoreLikeThisQueryBuilder implements SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder {

    private LuceneSearchEngine searchEngine;

    private LuceneSearchEngineFactory searchEngineFactory;

    private Reader reader;

    private Resource idResource;


    private LuceneResource resource;

    private MoreLikeThis moreLikeThis;

    private String[] subIndexes;

    private String[] aliases;

    private boolean propertiesSet;

    public LuceneSearchEngineMoreLikeThisQueryBuilder(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory, Resource idResource) {
        this.idResource = idResource;
        this.resource = (LuceneResource) searchEngine.load(idResource);
        init(searchEngine, searchEngineFactory);
    }

    public LuceneSearchEngineMoreLikeThisQueryBuilder(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory, Reader reader) {
        this.reader = reader;
        init(searchEngine, searchEngineFactory);
    }

    private void init(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngine = searchEngine;
        this.searchEngineFactory = searchEngineFactory;
        LuceneSearchEngineInternalSearch internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        this.moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        this.moreLikeThis.setFieldNames(null);
        this.moreLikeThis.setAnalyzer(searchEngine.getSearchEngineFactory().getAnalyzerManager().getSearchAnalyzer());
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setSubIndexes(String[] subIndexes) {
        this.subIndexes = subIndexes;
        LuceneSearchEngineInternalSearch internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        MoreLikeThis moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        copy(moreLikeThis);
        this.moreLikeThis = moreLikeThis;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setAliases(String[] aliases) {
        this.aliases = aliases;
        LuceneSearchEngineInternalSearch internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        MoreLikeThis moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        copy(moreLikeThis);
        this.moreLikeThis = moreLikeThis;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setProperties(String[] properties) {
        propertiesSet = true;
        moreLikeThis.setFieldNames(properties);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder addProperty(String property) {
        propertiesSet = true;
        if (moreLikeThis.getFieldNames() == null) {
            moreLikeThis.setFieldNames(new String[]{property});
        } else {
            String[] newNames = new String[moreLikeThis.getFieldNames().length + 1];
            System.arraycopy(moreLikeThis.getFieldNames(), 0, newNames, 0, moreLikeThis.getFieldNames().length);
            newNames[newNames.length -1] = property;
            moreLikeThis.setFieldNames(newNames);
        }
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setAnalyzer(String analyzer) {
        moreLikeThis.setAnalyzer(searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerMustExist(analyzer));
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setBoost(boolean boost) {
        moreLikeThis.setBoost(boost);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxNumTokensParsed(int maxNumTokensParsed) {
        moreLikeThis.setMaxNumTokensParsed(maxNumTokensParsed);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxQueryTerms(int maxQueryTerms) {
        moreLikeThis.setMaxQueryTerms(maxQueryTerms);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxWordLen(int maxWordLen) {
        moreLikeThis.setMaxWordLen(maxWordLen);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinResourceFreq(int minDocFreq) {
        moreLikeThis.setMinDocFreq(minDocFreq);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinTermFreq(int minTermFreq) {
        moreLikeThis.setMinTermFreq(minTermFreq);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinWordLen(int minWordLen) {
        moreLikeThis.setMinWordLen(minWordLen);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setStopWords(String[] stopWords) {
        HashSet<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(stopWords));
        moreLikeThis.setStopWords(set);
        return this;
    }

    public SearchEngineQuery toQuery() {
        if (!propertiesSet) {
            moreLikeThis.setFieldNames(new String[]{searchEngine.getSearchEngineFactory().getAllProperty()});
        }
        try {
            Query query;
            if (reader != null) {
                query = moreLikeThis.like(reader);
            } else {
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add(moreLikeThis.like(resource.getDocNum()), BooleanClause.Occur.MUST);
                boolQuery.add(ResourceHelper.buildResourceLoadQuery(resource.getResourceKey()), BooleanClause.Occur.MUST_NOT);
                query = boolQuery;
            }
            return new LuceneSearchEngineQuery(searchEngineFactory, query);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to find queries like [" + idResource + "]", e);
        }
    }

    private void copy(MoreLikeThis moreLikeThis) {
        moreLikeThis.setAnalyzer(this.moreLikeThis.getAnalyzer());
        moreLikeThis.setBoost(this.moreLikeThis.isBoost());
        moreLikeThis.setFieldNames(this.moreLikeThis.getFieldNames());
        moreLikeThis.setMaxNumTokensParsed(this.moreLikeThis.getMaxNumTokensParsed());
        moreLikeThis.setMaxQueryTerms(this.moreLikeThis.getMaxQueryTerms());
        moreLikeThis.setMaxWordLen(this.moreLikeThis.getMaxWordLen());
        moreLikeThis.setMinDocFreq(this.moreLikeThis.getMinDocFreq());
        moreLikeThis.setMinTermFreq(this.moreLikeThis.getMinTermFreq());
        moreLikeThis.setMinWordLen(this.moreLikeThis.getMinWordLen());
        moreLikeThis.setStopWords(this.moreLikeThis.getStopWords());
    }
}
