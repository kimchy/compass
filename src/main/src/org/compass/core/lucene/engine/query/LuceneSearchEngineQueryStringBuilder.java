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

package org.compass.core.lucene.engine.query;

import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.util.LuceneQueryParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.ParseException;

/**
 * @author kimchy
 */
public class LuceneSearchEngineQueryStringBuilder implements SearchEngineQueryBuilder.SearchEngineQueryStringBuilder {

    private LuceneSearchEngine searchEngine;

    private Analyzer analyzer;

    private String defaultSearchProperty;

    private String queryString;

    private LuceneQueryParser.Operator operator = LuceneQueryParser.Operator.OR;

    public LuceneSearchEngineQueryStringBuilder(LuceneSearchEngine searchEngine, String queryString) {
        this.searchEngine = searchEngine;
        this.queryString = queryString;
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getSearchAnalyzer();
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setAnalyzer(String analyzer) {
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerMustExist(analyzer);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setAnalyzerByAlias(String alias) {
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerByAliasMustExists(alias);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty) {
        this.defaultSearchProperty = defaultSearchProperty;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useAndDefaultOperator() {
        this.operator = LuceneQueryParser.Operator.AND;
        return this;
    }

    public SearchEngineQuery toQuery() {
        String defaultSearch = defaultSearchProperty;
        if (defaultSearch == null) {
            defaultSearch = searchEngine.getSearchEngineFactory().getLuceneSettings().getDefaultSearchPropery();
        }
        Query qQuery;
        try {
            LuceneQueryParser queryParser = new LuceneQueryParser(defaultSearch, analyzer);
            queryParser.setDefaultOperator(operator);
            qQuery = queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
        return new LuceneSearchEngineQuery(searchEngine, qQuery);
    }
}
