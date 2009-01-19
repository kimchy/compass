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

package org.apache.lucene.queryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.compass.core.Property;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.all.AllBoostingTermQuery;
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

    private static final KeywordAnalyzer KEYWORD_ANALYZER = new KeywordAnalyzer();

    protected final CompassMapping mapping;

    protected final LuceneSearchEngineFactory searchEngineFactory;

    private boolean allowConstantScorePrefixQuery;

    private boolean addAliasQueryWithDotPath = true;

    private boolean forceAnalyzer;

    protected boolean suggestedQuery = false;

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
        if (searchEngineFactory.getAliasProperty().equals(field)) {
            return QueryParserUtils.createPolyAliasQuery(searchEngineFactory, queryText);
        }
        ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup(field);
        lookup.setConvertOnlyWithDotPath(false);
        if (lookup.hasSpecificConverter()) {
            queryText = lookup.attemptNormalizeString(queryText);

        }
        Analyzer origAnalyzer = analyzer;
        if (!forceAnalyzer) {
            String analyzerName = lookup.getAnalyzer();
            if (analyzerName != null) {
                analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerMustExist(analyzerName);
            } else {
                if (lookup.getResourcePropertyMapping() != null &&
                        (lookup.getResourcePropertyMapping().getIndex() == Property.Index.UN_TOKENIZED || lookup.getResourcePropertyMapping().getIndex() == Property.Index.NOT_ANALYZED)) {
                    analyzer = KEYWORD_ANALYZER;
                }
            }
        }
        try {
            return QueryParserUtils.andAliasQueryIfNeeded(getInternalFieldQuery(lookup.getPath(), queryText), lookup, addAliasQueryWithDotPath, searchEngineFactory);
        } finally {
            if (origAnalyzer != null) {
                analyzer = origAnalyzer;
            }
        }
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
                part1 = lookup.attemptNormalizeString(part1);
            }
            if ("*".equals(part2)) {
                part2 = null;
            } else {
                part2 = lookup.attemptNormalizeString(part2);
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


    // LUCENE MONITOR
    // Changed: Added boostAll flag
    // Extracted the creation of Terms to allow for overrides
    protected Query getInternalFieldQuery(String field, String queryText)  throws ParseException {
        boolean boostAll = false;
        if (searchEngineFactory.getLuceneSettings().isAllPropertyBoostSupport() &&
                field.equals(searchEngineFactory.getLuceneSettings().getAllProperty())) {
            boostAll = true;
        }
      // Use the analyzer to get all the tokens, and then build a TermQuery,
      // PhraseQuery, or nothing based on the term count

      TokenStream source = analyzer.tokenStream(field, new StringReader(queryText));
      List list = new ArrayList();
      final org.apache.lucene.analysis.Token reusableToken = new org.apache.lucene.analysis.Token();
      org.apache.lucene.analysis.Token nextToken;
      int positionCount = 0;
      boolean severalTokensAtSamePosition = false;

      while (true) {
        try {
          nextToken = source.next(reusableToken);
        }
        catch (IOException e) {
          nextToken = null;
        }
        if (nextToken == null)
          break;
        list.add(nextToken.clone());
        if (nextToken.getPositionIncrement() != 0)
          positionCount += nextToken.getPositionIncrement();
        else
          severalTokensAtSamePosition = true;
      }
      try {
        source.close();
      }
      catch (IOException e) {
        // ignore
      }

      if (list.size() == 0)
        return null;
      else if (list.size() == 1) {
        nextToken = (org.apache.lucene.analysis.Token) list.get(0);
          if (boostAll) {
              return new AllBoostingTermQuery(getTerm(field, nextToken.term()));
          } else {
              return newTermQuery(getTerm(field, nextToken.term()));
          }
      } else {
        if (severalTokensAtSamePosition) {
          if (positionCount == 1) {
            // no phrase query:
            BooleanQuery q = newBooleanQuery(true);
            for (int i = 0; i < list.size(); i++) {
              nextToken = (org.apache.lucene.analysis.Token) list.get(i);
                if (boostAll) {
                    AllBoostingTermQuery currentQuery = new AllBoostingTermQuery(
                            getTerm(field, nextToken.term()));
                    q.add(currentQuery, BooleanClause.Occur.SHOULD);
                } else {
                  Query currentQuery = newTermQuery(
                      getTerm(field, nextToken.term()));
                  q.add(currentQuery, BooleanClause.Occur.SHOULD);
                }
            }
            return q;
          }
          else {
            // phrase query:
            MultiPhraseQuery mpq = newMultiPhraseQuery();
            mpq.setSlop(phraseSlop);
            List multiTerms = new ArrayList();
            int position = -1;
            for (int i = 0; i < list.size(); i++) {
              nextToken = (org.apache.lucene.analysis.Token) list.get(i);
              if (nextToken.getPositionIncrement() > 0 && multiTerms.size() > 0) {
                if (enablePositionIncrements) {
                  mpq.add((Term[])multiTerms.toArray(new Term[0]),position);
                } else {
                  mpq.add((Term[])multiTerms.toArray(new Term[0]));
                }
                multiTerms.clear();
              }
              position += nextToken.getPositionIncrement();
              multiTerms.add(getTerm(field, nextToken.term()));
            }
            if (enablePositionIncrements) {
              mpq.add((Term[])multiTerms.toArray(new Term[0]),position);
            } else {
              mpq.add((Term[])multiTerms.toArray(new Term[0]));
            }
            return mpq;
          }
        }
        else {
          PhraseQuery pq = newPhraseQuery();
          pq.setSlop(phraseSlop);
          int position = -1;
          for (int i = 0; i < list.size(); i++) {
            nextToken = (org.apache.lucene.analysis.Token) list.get(i);
            if (enablePositionIncrements) {
              position += nextToken.getPositionIncrement();
              pq.add(getTerm(field, nextToken.term()),position);
            } else {
              pq.add(getTerm(field, nextToken.term()));
            }
          }
          return pq;
        }
      }
    }

    protected Term getTerm(String field, String text) throws ParseException {
        return new Term(field, text);
    }

    public void close() {
        
    }

    public boolean isSuggestedQuery() {
        return suggestedQuery;
    }
}
