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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class LuceneSearchEngineMultiPropertyQueryStringBuilder implements SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder {

    private LuceneSearchEngineFactory searchEngineFactory;

    private Analyzer analyzer;

    private boolean forceAnalyzer;

    private String queryString;

    private QueryParser.Operator operator;

    private ArrayList<String> propertyNames = new ArrayList<String>();

    private Map<String, Float> boosts = new HashMap<String, Float>();

    private LuceneQueryParser queryParser;

    public LuceneSearchEngineMultiPropertyQueryStringBuilder(LuceneSearchEngineFactory searchEngineFactory , String queryString) {
        this.searchEngineFactory = searchEngineFactory;
        this.queryString = queryString;
        this.analyzer = searchEngineFactory.getAnalyzerManager().getSearchAnalyzer();
        this.queryParser = searchEngineFactory.getQueryParserManager().getDefaultQueryParser();
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder setAnalyzer(String analyzer) {
        this.analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerMustExist(analyzer);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder setAnalyzerByAlias(String alias) {
        this.analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerByAliasMustExists(alias);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder add(String resourcePropertyName) {
        propertyNames.add(resourcePropertyName);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder add(String resourcePropertyName, float boost) {
        propertyNames.add(resourcePropertyName);
        boosts.put(resourcePropertyName, new Float(boost));
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder useAndDefaultOperator() {
        this.operator = QueryParser.Operator.AND;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder useOrDefaultOperator() {
        this.operator = QueryParser.Operator.OR;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder forceAnalyzer() {
        this.forceAnalyzer = true;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder setQueryParser(String queryParser) {
        this.queryParser = searchEngineFactory.getQueryParserManager().getQueryParser(queryParser);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder useSpellCheck() {
        return setQueryParser(LuceneEnvironment.QueryParser.SPELLCHECK_GROUP);
    }


    public SearchEngineQuery toQuery() {
        QueryHolder qQuery = queryParser.parse(propertyNames.toArray(new String[propertyNames.size()]), boosts,
                operator, analyzer, forceAnalyzer, queryString);
        return new LuceneSearchEngineQuery(searchEngineFactory, qQuery);
    }
}
