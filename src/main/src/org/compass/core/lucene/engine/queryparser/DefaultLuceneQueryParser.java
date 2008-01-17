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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CompassMultiFieldQueryParser;
import org.apache.lucene.queryParser.CompassQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.CompassMapping;

/**
 * The deafult Lucene query parser. Uses {@link org.compass.core.lucene.LuceneEnvironment.QueryParser#DEFAULT_PARSER_ALLOW_LEADING_WILDCARD}
 *
 *
 * @author kimchy
 */
public class DefaultLuceneQueryParser implements LuceneQueryParser, CompassMappingAware, SearchEngineFactoryAware, CompassConfigurable {

    private static Log log = LogFactory.getLog(DefaultLuceneQueryParser.class);

    private CompassMapping mapping;

    private SearchEngineFactory searchEngineFactory;

    private boolean allowLeadingWildcard;

    private boolean allowConstantScorePrefixQuery;

    public void configure(CompassSettings settings) throws CompassException {
        allowLeadingWildcard = settings.getSettingAsBoolean(LuceneEnvironment.QueryParser.DEFAULT_PARSER_ALLOW_LEADING_WILDCARD, false);
        allowConstantScorePrefixQuery = settings.getSettingAsBoolean(LuceneEnvironment.QueryParser.DEFAULT_PARSER_ALLOW_CONSTANT_SCORE_PREFIX_QUERY, true);
        if (log.isDebugEnabled()) {
            log.debug("Query Parser configured with allowLeadingWildcard [" + allowLeadingWildcard + "] and allowConstantScorePrefixQuery [" + allowConstantScorePrefixQuery + "]");
        }
    }

    public void setCompassMapping(CompassMapping mapping) {
        this.mapping = mapping;
    }

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public Query parse(String property, QueryParser.Operator operator, Analyzer analyzer, boolean forceAnalyzer, String queryString) throws SearchEngineQueryParseException {
        CompassQueryParser queryParser = new CompassQueryParser(property, analyzer, mapping, searchEngineFactory, forceAnalyzer);
        queryParser.setDefaultOperator(operator);
        queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
        queryParser.setAllowConstantScorePrefixQuery(allowConstantScorePrefixQuery);
        try {
            return queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
    }

    public Query parse(String[] properties, QueryParser.Operator operator, Analyzer analyzer, boolean forceAnalyzer, String queryString) throws SearchEngineQueryParseException {
        CompassMultiFieldQueryParser queryParser = new CompassMultiFieldQueryParser(properties, analyzer, mapping, searchEngineFactory, forceAnalyzer);
        queryParser.setDefaultOperator(operator);
        queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
        queryParser.setAllowConstantScorePrefixQuery(allowConstantScorePrefixQuery);
        try {
            return queryParser.parse(queryString);
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        }
    }
}
