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

package org.apache.lucene.queryParser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.queryparser.QueryParserUtils;
import org.compass.core.lucene.search.ConstantScorePrefixQuery;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyLookup;

/**
 * Extends Lucene {@link org.apache.lucene.queryParser.QueryParser} and overrides {@link #getRangeQuery(String,String,String,boolean)}
 * since lucene performs data parsing which is a performance killer. Anyhow, handling dates in Compass
 * is different and simpler than Lucene.
 *
 * @author kimchy
 */
public class CompassQueryParser extends QueryParser {

    private CompassMapping mapping;

    private LuceneSearchEngineFactory searchEngineFactory;

    private boolean allowConstantScorePrefixQuery;

    private boolean addAliasQueryWithDotPath = true;

    private boolean forceAnalyzer;

    public CompassQueryParser(String f, Analyzer a, CompassMapping mapping, SearchEngineFactory searchEngineFactory, boolean forceAnalyzer) {
        super(f, a);
        this.mapping = mapping;
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
        this.forceAnalyzer = forceAnalyzer;
    }

    public void setAllowConstantScorePrefixQuery(boolean allowConstantScorePrefixQuery) {
        this.allowConstantScorePrefixQuery = allowConstantScorePrefixQuery;
    }

    public void setAddAliasQueryWithDotPath(boolean addAliasQueryWithDotPath) {
        this.addAliasQueryWithDotPath = addAliasQueryWithDotPath;
    }

    protected Query getWildcardQuery(String field, String termStr) throws ParseException {
        ResourcePropertyLookup lookup = null;
        if (field != null) {
            lookup = mapping.getResourcePropertyLookup(field);
            lookup.setConvertOnlyWithDotPath(false);
            field = lookup.getPath();
        }
        return QueryParserUtils.andAliasQueryIfNeeded(super.getWildcardQuery(field, termStr), lookup, addAliasQueryWithDotPath, searchEngineFactory);
    }

    protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException {
        ResourcePropertyLookup lookup = null;
        if (field != null) {
            lookup = mapping.getResourcePropertyLookup(field);
            lookup.setConvertOnlyWithDotPath(false);
            field = lookup.getPath();
        }
        return QueryParserUtils.andAliasQueryIfNeeded(super.getFuzzyQuery(field, termStr, minSimilarity), lookup, addAliasQueryWithDotPath, searchEngineFactory);
    }

    protected Query getFieldQuery(String field, String queryText) throws ParseException {
        if (field == null) {
            return super.getFieldQuery(field, queryText);
        }
        ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup(field);
        lookup.setConvertOnlyWithDotPath(false);
        if (lookup.hasSpecificConverter()) {
            queryText = lookup.normalizeString(queryText);

        }
        Analyzer origAnalyzer = null;
        if (!forceAnalyzer) {
            String analyzerName = lookup.getAnalyzer();
            if (analyzerName != null) {
                origAnalyzer = analyzer;
                analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerMustExist(analyzerName);
            }
        }
        Query q = QueryParserUtils.andAliasQueryIfNeeded(super.getFieldQuery(lookup.getPath(), queryText), lookup, addAliasQueryWithDotPath, searchEngineFactory);
        if (origAnalyzer != null) {
            analyzer = origAnalyzer;
        }
        return q;
    }

    /**
     * Override it so we won't use the date format to try and parse dates
     */
    protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException {
        if (getLowercaseExpandedTerms()) {
            part1 = part1.toLowerCase();
            part2 = part2.toLowerCase();
        }

        ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup(field);
        lookup.setConvertOnlyWithDotPath(false);
        if (lookup.hasSpecificConverter()) {
            if ("*".equals(part1)) {
                part1 = null;
            } else {
                part1 = lookup.normalizeString(part1);
            }
            if ("*".equals(part2)) {
                part2 = null;
            } else {
                part2 = lookup.normalizeString(part2);
            }
        } else {
            if ("*".equals(part1)) {
                part1 = null;
            }
            if ("*".equals(part2)) {
                part2 = null;
            }
        }

        return QueryParserUtils.andAliasQueryIfNeeded(new ConstantScoreRangeQuery(lookup.getPath(), part1, part2, inclusive, inclusive), lookup, addAliasQueryWithDotPath, searchEngineFactory);
    }

    protected Query getPrefixQuery(String field, String termStr) throws ParseException {
        ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup(field);
        lookup.setConvertOnlyWithDotPath(false);

        if (!allowConstantScorePrefixQuery) {
            return super.getPrefixQuery(lookup.getPath(), termStr);
        }

        if (getLowercaseExpandedTerms()) {
            termStr = termStr.toLowerCase();
        }

        Term t = new Term(lookup.getPath(), termStr);
        return QueryParserUtils.andAliasQueryIfNeeded(new ConstantScorePrefixQuery(t), lookup, addAliasQueryWithDotPath, searchEngineFactory);
    }
}