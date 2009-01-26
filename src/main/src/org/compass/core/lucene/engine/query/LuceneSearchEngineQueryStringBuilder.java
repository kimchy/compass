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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.queryparser.LuceneQueryParser;
import org.compass.core.lucene.engine.queryparser.QueryHolder;

/**
 * @author kimchy
 */
public class LuceneSearchEngineQueryStringBuilder implements SearchEngineQueryBuilder.SearchEngineQueryStringBuilder {

    private LuceneSearchEngineFactory searchEngineFactory;

    private Analyzer analyzer;

    private String defaultSearchProperty;

    private String queryString;

    private QueryParser.Operator operator;

    private LuceneQueryParser queryParser;

    private boolean forceAnalyzer;

    public LuceneSearchEngineQueryStringBuilder(LuceneSearchEngineFactory searchEngineFactory, String queryString) {
        this.searchEngineFactory = searchEngineFactory;
        this.queryString = queryString;
        this.analyzer = searchEngineFactory.getAnalyzerManager().getSearchAnalyzer();
        this.queryParser = searchEngineFactory.getQueryParserManager().getDefaultQueryParser();
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setAnalyzer(String analyzer) {
        this.analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerMustExist(analyzer);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setAnalyzerByAlias(String alias) {
        this.analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerByAliasMustExists(alias);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty) {
        this.defaultSearchProperty = defaultSearchProperty;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useAndDefaultOperator() {
        this.operator = QueryParser.Operator.AND;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useOrDefaultOperator() {
        this.operator = QueryParser.Operator.OR;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder forceAnalyzer() {
        this.forceAnalyzer = true;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder setQueryParser(String queryParser) {
        this.queryParser = searchEngineFactory.getQueryParserManager().getQueryParser(queryParser);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useSpellCheck() {
        return setQueryParser(LuceneEnvironment.QueryParser.SPELLCHECK_GROUP);
    }

    public SearchEngineQuery toQuery() {
        String defaultSearch = defaultSearchProperty;
        if (defaultSearch == null) {
            defaultSearch = searchEngineFactory.getLuceneSettings().getDefaultSearchPropery();
        }
        QueryHolder qQuery = queryParser.parse(defaultSearch, operator, analyzer, forceAnalyzer, queryString);
        return new LuceneSearchEngineQuery(searchEngineFactory, qQuery, defaultSearch);
    }
}
