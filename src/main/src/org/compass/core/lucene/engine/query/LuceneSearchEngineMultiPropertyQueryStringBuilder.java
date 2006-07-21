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

import java.util.ArrayList;

import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.util.LuceneQueryParser;
import org.compass.core.lucene.util.LuceneMultiFieldQueryParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.ParseException;

/**
 * @author kimchy
 */
public class LuceneSearchEngineMultiPropertyQueryStringBuilder implements SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder {

    private LuceneSearchEngine searchEngine;

    private Analyzer analyzer;

    private String queryString;

    private LuceneQueryParser.Operator operator = LuceneQueryParser.Operator.OR;

    private ArrayList propertyNames = new ArrayList();

    public LuceneSearchEngineMultiPropertyQueryStringBuilder(LuceneSearchEngine searchEngine, String queryString) {
        this.searchEngine = searchEngine;
        this.queryString = queryString;
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getSearchAnalyzer();
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder setAnalyzer(String analyzer) {
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerMustExist(analyzer);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder setAnalyzerByAlias(String alias) {
        this.analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerByAliasMustExists(alias);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder add(String resourcePropertyName) {
        propertyNames.add(resourcePropertyName);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder useAndDefaultOperator() {
        this.operator = LuceneQueryParser.Operator.AND;
        return this;
    }

    public SearchEngineQuery toQuery() {
        Query qQuery;
        try {
            LuceneMultiFieldQueryParser queryParser = new LuceneMultiFieldQueryParser(
                    (String[]) propertyNames.toArray(new String[propertyNames.size()]), analyzer);
            queryParser.setDefaultOperator(operator);
            qQuery = queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
        return new LuceneSearchEngineQuery(searchEngine, qQuery);
    }
}
