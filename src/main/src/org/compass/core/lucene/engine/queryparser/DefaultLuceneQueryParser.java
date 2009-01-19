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

package org.compass.core.lucene.engine.queryparser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CompassMultiFieldQueryParser;
import org.apache.lucene.queryParser.CompassQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.CompassMapping;

/**
 * The deafult Lucene query parser. Uses {@link org.compass.core.lucene.LuceneEnvironment.QueryParser#DEFAULT_PARSER_ALLOW_LEADING_WILDCARD}
 *
 * @author kimchy
 */
public class DefaultLuceneQueryParser implements LuceneQueryParser, CompassMappingAware, SearchEngineFactoryAware, CompassConfigurable {

    private static Log log = LogFactory.getLog(DefaultLuceneQueryParser.class);

    private CompassMapping mapping;

    private SearchEngineFactory searchEngineFactory;

    private boolean allowLeadingWildcard;

    private boolean allowConstantScorePrefixQuery;

    private float fuzzyMinSimilarity;

    private int fuzzyPrefixLength;

    private QueryParser.Operator defaultOperator;

    public void configure(CompassSettings settings) throws CompassException {
        allowLeadingWildcard = settings.getSettingAsBoolean(LuceneEnvironment.QueryParser.DEFAULT_PARSER_ALLOW_LEADING_WILDCARD, true);
        allowConstantScorePrefixQuery = settings.getSettingAsBoolean(LuceneEnvironment.QueryParser.DEFAULT_PARSER_ALLOW_CONSTANT_SCORE_PREFIX_QUERY, true);
        fuzzyMinSimilarity = settings.getSettingAsFloat(LuceneEnvironment.QueryParser.DEFAULT_PARSER_FUZZY_MIN_SIMILARITY, FuzzyQuery.defaultMinSimilarity);
        fuzzyPrefixLength = settings.getSettingAsInt(LuceneEnvironment.QueryParser.DEFAULT_PARSER_FUZZY_PERFIX_LENGTH, FuzzyQuery.defaultPrefixLength);
        String sDefaultOperator = settings.getSetting(LuceneEnvironment.QueryParser.DEFAULT_PARSER_DEFAULT_OPERATOR, "AND");
        if ("and".equalsIgnoreCase(sDefaultOperator)) {
            defaultOperator = QueryParser.Operator.AND;
        } else if ("or".equalsIgnoreCase(sDefaultOperator)) {
            defaultOperator = QueryParser.Operator.OR;
        } else {
            throw new ConfigurationException("Defualt query string operator [" + sDefaultOperator + "] not recognized.");
        }
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

    public QueryHolder parse(String property, QueryParser.Operator operator, Analyzer analyzer, boolean forceAnalyzer, String queryString) throws SearchEngineQueryParseException {
        CompassQueryParser queryParser = createQueryParser(property, analyzer, forceAnalyzer);
        queryParser.setDefaultOperator(getOperator(operator));
        queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
        queryParser.setAllowConstantScorePrefixQuery(allowConstantScorePrefixQuery);
        queryParser.setFuzzyMinSim(fuzzyMinSimilarity);
        queryParser.setFuzzyPrefixLength(fuzzyPrefixLength);
        try {
            Query query = queryParser.parse(queryString);
            return new QueryHolder(query, queryParser.isSuggestedQuery());
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        } catch (IllegalArgumentException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        } finally {
            queryParser.close();
        }
    }

    public QueryHolder parse(String[] properties, QueryParser.Operator operator, Analyzer analyzer, boolean forceAnalyzer, String queryString) throws SearchEngineQueryParseException {
        CompassMultiFieldQueryParser queryParser = createMultiQueryParser(properties, analyzer, forceAnalyzer);
        queryParser.setDefaultOperator(getOperator(operator));
        queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
        queryParser.setAllowConstantScorePrefixQuery(allowConstantScorePrefixQuery);
        queryParser.setFuzzyMinSim(fuzzyMinSimilarity);
        queryParser.setFuzzyPrefixLength(fuzzyPrefixLength);
        try {
            Query query = queryParser.parse(queryString);
            return new QueryHolder(query, queryParser.isSuggestedQuery());
        } catch (ParseException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        } catch (IllegalArgumentException e) {
            throw new SearchEngineQueryParseException(queryString, e);
        } finally {
            queryParser.close();
        }
    }

    private QueryParser.Operator getOperator(QueryParser.Operator operator) {
        if (operator == null) {
            return defaultOperator;
        }
        return operator;
    }

    protected CompassMapping getMapping() {
        return mapping;
    }

    protected SearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    protected CompassQueryParser createQueryParser(String property, Analyzer analyzer, boolean forceAnalyzer) {
        return new CompassQueryParser(property, analyzer, mapping, searchEngineFactory, forceAnalyzer);
    }

    protected CompassMultiFieldQueryParser createMultiQueryParser(String[] properties, Analyzer analyzer, boolean forceAnalyzer) {
        return new CompassMultiFieldQueryParser(properties, analyzer, mapping, searchEngineFactory, forceAnalyzer);
    }

}
