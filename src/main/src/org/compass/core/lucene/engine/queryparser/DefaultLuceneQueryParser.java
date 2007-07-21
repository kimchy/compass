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

package org.compass.core.lucene.engine.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.mapping.CompassMapping;

/**
 * @author kimchy
 */
public class DefaultLuceneQueryParser implements LuceneQueryParser, CompassMappingAware {

    private CompassMapping mapping;

    public void setCompassMapping(CompassMapping mapping) {
        this.mapping = mapping;
    }

    public Query parse(String property, QueryParser.Operator operator, Analyzer analyzer, String queryString) throws SearchEngineQueryParseException {
        QueryParser queryParser = new CompassQueryParser(property, analyzer, mapping);
        queryParser.setDefaultOperator(operator);
        try {
            return queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
    }

    public Query parse(String[] properties, QueryParser.Operator operator, Analyzer analyzer, String queryString) throws SearchEngineQueryParseException {
        QueryParser queryParser = new CompassMultiFieldQueryParser(properties, analyzer, mapping);
        queryParser.setDefaultOperator(operator);
        try {
            return queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
    }
}
