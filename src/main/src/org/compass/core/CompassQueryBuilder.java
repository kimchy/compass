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

package org.compass.core;

import java.io.Reader;
import java.io.Serializable;

import org.compass.core.CompassQuery.CompassSpanQuery;

/**
 * <p>The query builder is used to construct {@link CompassQuery} programmatically.
 * Simple queries, like {@link #le(String,Object)}, will generate a {@link CompassQuery}.
 * More complex ones, will return their respective builder to continue and bulid them (like
 * {@link #multiPhrase(String)}). Combining {@link CompassQuery}s can be done using
 * the {@link #bool()} operation.
 *
 * <p>An example of building a query using the query builder:
 * <pre>
 * CompassQueryBuilder queryBuilder = session.queryBuilder();
 * queryBuilder.bool().addMust(queryBuilder.term(&quot;name&quot;, &quot;jack&quot;)).addMust(queryBuilder.lt(&quot;birthdate&quot;, &quot;19500101&quot;))
 *      .toQuery().hits();
 * </pre>
 *
 * @author kimchy
 */
public interface CompassQueryBuilder {

    /**
     * A general interface for internal builders that will create a
     * {@link CompassQuery}.
     *
     * @author kimchy
     */
    public static interface ToCompassQuery {

        /**
         * Create the query.
         */
        CompassQuery toQuery();
    }

    /**
     * A boolean query builder. Used to construct query that will return hits
     * that are the matching boolean combinations of other queries.
     *
     * @author kimchy
     */
    public static interface CompassBooleanQueryBuilder extends ToCompassQuery {

        /**
         * Hits must match the given query.
         *
         * @param query The query to add
         * @return The current builder
         */
        CompassBooleanQueryBuilder addMust(CompassQuery query);

        /**
         * Hits must not match the given query. Note that it is not possible to
         * build a boolean query that only consists of must not queries.
         *
         * @param query The query to add
         * @return The current builder
         */
        CompassBooleanQueryBuilder addMustNot(CompassQuery query);

        /**
         * Hits should match the given query. For a boolean build query with two
         * <code>should</code> subqueries, at least one of the queries must
         * appear in the matching hits.
         *
         * @param query The query to add
         * @return The current builder
         */
        CompassBooleanQueryBuilder addShould(CompassQuery query);

        /**
         * Specifies a minimum number of the optional BooleanClauses
         * which must be satisfied.
         *
         * <p>By default no optional clauses are necessary for a match
         * (unless there are no required clauses).  If this method is used,
         * then the specified number of clauses is required.
         *
         * <p>Use of this method is totally independent of specifying that
         * any specific clauses are required (or prohibited).  This number will
         * only be compared against the number of matching optional clauses.
         *
         * <p>EXPERT NOTE: Using this method may force collecting docs in order,
         * regardless of whether setAllowDocsOutOfOrder(true) has been called.
         */
        CompassBooleanQueryBuilder setMinimumNumberShouldMatch(int min);
    }

    /**
     * A query builder that constructs a phrase query. A phrase query is used to
     * locate hits with terms within a certain distance from one another. The
     * distance is also called slop. For example, you can use it to search for
     * the values: java and london, which are near one another. "Near" is
     * measured using the slop, and a value of 1 means that they will be in a
     * distance of 1 other value from one another.
     * <p/>
     * The slop defaults to 0.
     *
     * @author kimchy
     */
    public static interface CompassMultiPhraseQueryBuilder extends ToCompassQuery {

        /**
         * Sets the slop for the phrase query.
         */
        CompassMultiPhraseQueryBuilder setSlop(int slop);

        /**
         * Adds a single value to the next position in the phrase.
         */
        CompassMultiPhraseQueryBuilder add(Object value);

        /**
         * Adds a single value to the position given in the phrase.
         */
        CompassMultiPhraseQueryBuilder add(Object value, int position);

        /**
         * Adds several values to the next position in the phrase.
         */
        CompassMultiPhraseQueryBuilder add(Object[] values);

        /**
         * Adds several values to the position given in the phrase.
         */
        CompassMultiPhraseQueryBuilder add(Object[] values, int position);
    }

    /**
     * A query builder used to construct a query from a query string (i.e. +jack
     * +fang). The analyzer that will be used to analyze the query string and
     * the default search property (for search terms not prefixed with a
     * property name) can be set before calling the <code>toQuery</code>.
     *
     * @author kimchy
     */
    public static interface CompassQueryStringBuilder extends ToCompassQuery {

        /**
         * Sets the analyzer that will be used to analyze the query string. Can
         * be <code>null</code>. It is used when parsing a query string and
         * has no effect when using a bulit in query (using the {@link CompassQuery}).
         */
        CompassQueryStringBuilder setAnalyzer(String analyzer) throws CompassException;

        /**
         * Sets te query parser lookup name that will be used to parse the query string.
         */
        CompassQueryStringBuilder setQueryParser(String queryParser) throws CompassException;

        /**
         * Uses the spell check for suggesting a query based on the query string.
         */
        CompassQueryStringBuilder useSpellCheck() throws CompassException;

        /**
         * Sets the analyzer that will be used to analyzer the query string. The
         * analyzer will be built based on analyzer settings for the mapping definitions
         * the define the alias. It means that if a certain property is associated with
         * a specific analyzer, a per property analyzer will be built.
         */
        CompassQueryStringBuilder setAnalyzerByAlias(String alias) throws CompassException;

        /**
         * Sets the default search property for non prefixed terms in the query
         * string. Can be <code>null</code>. It is used when parsing a query
         * string and has no effect when using a bulit in query (using the
         * {@link CompassQuery}).
         */
        CompassQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty);

        /**
         * Uses the and operator as the default operator instead of OR operator.
         */
        CompassQueryStringBuilder useAndDefaultOperator();

        /**
         * Uses the OR operator as the default operator instead of AND operator.
         */
        CompassQueryStringBuilder useOrDefaultOperator();

        /**
         * Forces the query string to only use the analyzer specificed (or configured)
         * and not take into account any analyzers that might be specifiec within the mappings.
         */
        CompassQueryStringBuilder forceAnalyzer();
    }

    /**
     * Parses the query string into terms, which all of them are used against the given
     * resource property name / meta-data.
     * <p/>
     * If the query string breaks into two terms (term1 and term2), and we use {@link #add(String)}
     * to add two resource property names: title and body, the query will be expanded to:
     * <code>(title:term1 body:term1) (title:term2 body:term2)</code>. If {@link #useAndDefaultOperator()}
     * is called, the query will be: <code>+(title:term1 body:term1) +(title:term2 body:term2)</code>.
     *
     * @author kimchy
     */
    public static interface CompassMultiPropertyQueryStringBuilder extends ToCompassQuery {

        /**
         * Sets the analyzer that will be used to analyze the query string. Can
         * be <code>null</code>. It is used when parsing a query string and
         * has no effect when using a bulit in query (using the {@link CompassQuery}).
         */
        CompassMultiPropertyQueryStringBuilder setAnalyzer(String analyzer);

        /**
         * Sets the analyzer that will be used to analyzer the query string. The
         * analyzer will be build based on analyzer settings for the mapping definitions
         * the define the alias. It means that if a certain property is associated with
         * a specific analyzer, a per property analyzer will be built.
         */
        CompassMultiPropertyQueryStringBuilder setAnalyzerByAlias(String alias) throws CompassException;

        /**
         * Sets te query parser lookup name that will be used to parse the query string.
         */
        CompassMultiPropertyQueryStringBuilder setQueryParser(String queryParser) throws CompassException;

        /**
         * Uses the spell check for suggesting a query based on the query string.
         */
        CompassMultiPropertyQueryStringBuilder useSpellCheck();

        /**
         * Adds another resource property name / meta-data that the query string will be executed against.
         * <p/>
         * The name can either be the actual resource property or meta-data value,
         * or the path to the given resource property (alias.rProperty), or the
         * class property (alias.cProperty) or the path to the meta-data
         * (alias.cProperty.metaData)
         *
         * @param name The name of the resource property / meta-data.
         */
        CompassMultiPropertyQueryStringBuilder add(String name);

        /**
         * If called, the query will be expanded to: <code>+(title:term1 body:term1) +(title:term2 body:term2)</code>
         * (Instead of <code>(title:term1 body:term1) (title:term2 body:term2)</code>).
         */
        CompassMultiPropertyQueryStringBuilder useAndDefaultOperator();

        /**
         * Uses the OR operator as the default operator instead of AND operator.
         */
        CompassMultiPropertyQueryStringBuilder useOrDefaultOperator();

        /**
         * Forces the query parser to use the analyzer specified or confiugred and not
         * analyzers that might be defined on different mappings.
         */
        CompassMultiPropertyQueryStringBuilder forceAnalyzer();
    }

    /**
     * A span near query builder. Matches spans which are near one another. One
     * can specify <i>slop</i>, the maximum number of intervening unmatched
     * positions, as well as whether matches are required to be in-order.
     * <p/>
     * <code>slop</code> defauls to <code>0</code> and <code>inOrder</code>
     * defaults to <code>true</code>.
     *
     * @author kimchy
     */
    public static interface CompassQuerySpanNearBuilder {

        /**
         * Sets the slop which is the distance allowed between spans.
         */
        CompassQuerySpanNearBuilder setSlop(int slop);

        /**
         * Sets if the spans need to be in order.
         */
        CompassQuerySpanNearBuilder setInOrder(boolean inOrder);

        /**
         * Adds a single value to the next span match.
         */
        CompassQuerySpanNearBuilder add(Object value);

        /**
         * Adds a single span query to the next span match.
         */
        CompassQuerySpanNearBuilder add(CompassSpanQuery query);

        /**
         * Returns the span near generated query.
         */
        CompassSpanQuery toQuery();
    }

    /**
     * Creates a span or query builder.
     *
     * @author kimchy
     */
    public static interface CompassQuerySpanOrBuilder {

        /**
         * Adds a span query which is or'ed with the rest of the added span
         * queries.
         */
        CompassQuerySpanOrBuilder add(CompassSpanQuery query);

        /**
         * Returns the generated span or query.
         */
        CompassSpanQuery toQuery();
    }

    /**
     * A more like this query builder (maps to Lucene <code>MoreLikeThis</code> feature withing
     * the contrib queries package).
     */
    public static interface CompassMoreLikeThisQuery extends ToCompassQuery {

        /**
         * Sets the sub indexes that "more liket this" hits will be searched on
         */
        CompassMoreLikeThisQuery setSubIndexes(String[] subIndexes);

        /**
         * Sets the aliases that "more liket this" hits will be searched on
         */
        CompassMoreLikeThisQuery setAliases(String[] aliases);

        /**
         * Sets properties to the more like this query will be performed on.
         */
        CompassMoreLikeThisQuery setProperties(String[] properties);

        /**
         * Adds a property to the more like this query will be performed on.
         */
        CompassMoreLikeThisQuery addProperty(String property);

        /**
         * Sets the analyzer that will be used to analyze a more like this string (used when
         * using {@link CompassQueryBuilder#moreLikeThis(java.io.Reader)}.
         */
        CompassMoreLikeThisQuery setAnalyzer(String analyzer);

        /**
         * Sets whether to boost terms in query based on "score" or not.
         */
        CompassMoreLikeThisQuery setBoost(boolean boost);

        /**
         * The maximum number of tokens to parse in each example doc field that is not stored with TermVector support
         */
        CompassMoreLikeThisQuery setMaxNumTokensParsed(int maxNumTokensParsed);

        /**
         * Sets the maximum number of query terms that will be included in any generated query.
         */
        CompassMoreLikeThisQuery setMaxQueryTerms(int maxQueryTerms);

        /**
         * Sets the maximum word length above which words will be ignored. Set this to 0 for no
         * maximum word length. The default is <code>0</code>.
         */
        CompassMoreLikeThisQuery setMaxWordLen(int maxWordLen);

        /**
         * Sets the minimum word length below which words will be ignored. Set this to 0 for no
         * minimum word length. The default is <code>0</code>.
         */
        CompassMoreLikeThisQuery setMinWordLen(int minWordLen);

        /**
         * Sets the frequency at which words will be ignored which do not occur in at least this
         * many resources. Defaults to 5.
         */
        CompassMoreLikeThisQuery setMinResourceFreq(int minDocFreq);

        /**
         * Sets the frequency below which terms will be ignored in the source doc. Defaults to 2.
         */
        CompassMoreLikeThisQuery setMinTermFreq(int minTermFreq);

        /**
         * Set the set of stopwords.
         * Any word in this set is considered "uninteresting" and ignored.
         * Even if your Analyzer allows stopwords, you might want to tell the MoreLikeThis code to ignore them, as
         * for the purposes of document similarity it seems reasonable to assume that "a stop word is never interesting".
         */
        CompassMoreLikeThisQuery setStopWords(String[] stopWords);

    }

    /**
     * Should Compass use a converter for value passed even if there is no specific direct dot
     * path notation to it. It will try and derive the best converter to use. Defaults to <code>false</code>.
     *
     * @see org.compass.core.mapping.ResourcePropertyLookup#setConvertOnlyWithDotPath(boolean) 
     */
    CompassQueryBuilder convertOnlyWithDotPath(boolean convertOnlyWithDotPath);

    /**
     * Should the query builder wrap automatically any query that has dot path notation (such as
     * <code>alias.property</code>) with specific narrowing to match the given alias. Default to
     * <code>true</code>.
     */
    CompassQueryBuilder addAliasQueryIfNeeded(boolean addAliasQueryIfNeeded);

    /**
     * Constructs a boolean query builder.
     */
    CompassBooleanQueryBuilder bool();

    /**
     * Constructs a boolean query builder, with coord disabled.
     */
    CompassBooleanQueryBuilder bool(boolean disableCoord);

    /**
     * Constructs a multi phrase query builder for the given resource property /
     * meta-data name.
     * <p/>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name The name of the resource property / meta-data.
     * @return The multi phrase query builder.
     */
    CompassMultiPhraseQueryBuilder multiPhrase(String name);

    /**
     * Constructs a query string query builder.
     *
     * @param queryString The query string (i.e. +jack +london).
     * @return The query string query builder.
     */
    CompassQueryStringBuilder queryString(String queryString);

    /**
     * Constructs a multi property query string builder, allowing to execute query strings
     * against several resource property names.
     *
     * @param queryString The query string (i.e. +jack +london)
     * @return The multi property string query builder.
     */
    CompassMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString);

    /**
     * Returns a query that <b>exactly</b> match the given alias.
     *
     * <p>Note, this will <b>not</b> narrow down the search to specific sub indxes.
     * In order to do that, please use {@link org.compass.core.CompassQuery#setAliases(String[])}.
     *
     * @param aliasValue The alias value to match to.
     * @return The generated query.
     */
    CompassQuery alias(String aliasValue);

    /**
     * Returns a query that match the given alias or any extedning aliases.
     *
     * <p>Note, this will <b>not</b> narrow down the search to specific sub indxes.
     * In order to do that, please use {@link org.compass.core.CompassQuery#setAliases(String[])}.
     *
     * @param aliasValue The alias value to match to or any extending aliases.
     * @return The generated query.
     */
    CompassQuery polyAlias(String aliasValue);

    /**
     * <p>Creates a query where the resource property must have the given value.
     * Note, that the value itself will not be analyzed, but the text that was
     * indexed might have been (if <code>indexed</code>). The search is case
     * sensative.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The value that must match
     * @return The generated query
     */
    CompassQuery term(String name, Object value);

    /**
     * Creates a query that match all documents.
     *
     * @return The generated query
     */
    CompassQuery matchAll();

    /**
     * <p>Creates a query where the resource property is between the given values.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name          The resource property name
     * @param low           The low value limit
     * @param high          The high value limit
     * @param inclusive     If the values are inclusive or exclusive.
     * @param constantScore If the query will affect the score of the results. With all other range queries
     *                      it will default to <code>true</code>.
     * @return The generated query
     */
    CompassQuery between(String name, Object low, Object high, boolean inclusive, boolean constantScore);

    /**
     * <p>Creates a query where the resource property is between the given values.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name      The resource property name
     * @param low       The low value limit
     * @param high      The high value limit
     * @param inclusive If the values are inclusive or exclusive.
     * @return The generated query
     */
    CompassQuery between(String name, Object low, Object high, boolean inclusive);

    /**
     * <p>Creates a query where the resource property is less than (<) the given
     * value.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The high limit value
     * @return The generated query
     */
    CompassQuery lt(String name, Object value);

    /**
     * <p>Creates a query where the resource property is less or equal (<=) to the
     * given value.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The high limit value
     * @return The generated query
     */
    CompassQuery le(String name, Object value);

    /**
     * <p>Creates a query where the resource property is greater than (>) to the
     * given value.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The low limit value
     * @return The generated query
     */
    CompassQuery gt(String name, Object value);

    /**
     * <p>Creates a query where the resource property is greater or equal (>=) to
     * the given value.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The resource property name
     * @param value The low limit value
     * @return The generated query
     */
    CompassQuery ge(String name, Object value);

    /**
     * <p>Creates a query where the resource property values starts with the given
     * prefix.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name   the resource property name
     * @param prefix The prefix value
     * @return The generated query
     */
    CompassQuery prefix(String name, String prefix);

    /**
     * Creates a query where the resource property values match the given
     * wildcard. Supported wildcards are <code>*</code>, which matches any
     * character sequence (including the empty one), and <code>?</code>,
     * which matches any single character. Note this query can be slow, as it
     * needs to iterate over many terms. In order to prevent extremely slow
     * WildcardQueries, a Wildcard term should not start with one of the
     * wildcards <code>*</code> or <code>?</code>.
     *
     * @param name     The name
     * @param wildcard The wildcard expression
     * @return The generated query
     */
    CompassQuery wildcard(String name, String wildcard);

    /**
     * <p>Creates a fuzzy query for the given resource property and the value. The
     * similiarity measurement is based on the Levenshtein (edit distance)
     * algorithm. The minimumSimilarity defaults to 0.5 and prefixLength
     * defaults to 0.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The name
     * @param value The value
     * @return The generated query
     */
    CompassQuery fuzzy(String name, String value);

    /**
     * <p>Creates a fuzzy query for the given resource property and the value. The
     * similiarity measurement is based on the Levenshtein (edit distance)
     * algorithm. The prefixLength defaults to 0.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name              The name
     * @param value             The value
     * @param minimumSimilarity The minimum similarity, a value between 0.0 and 1.0
     * @return The generated query
     */
    CompassQuery fuzzy(String name, String value, float minimumSimilarity);

    /**
     * <p>Creates a fuzzy query for the given resource property and the value. The
     * similiarity measurement is based on the Levenshtein (edit distance)
     * algorithm.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name              The name
     * @param value             The value
     * @param minimumSimilarity The minimum similarity, a value between 0.0 and 1.0
     * @param prefixLength      The length of common (non-fuzzy) prefix
     * @return The generated query
     */
    CompassQuery fuzzy(String name, String value, float minimumSimilarity, int prefixLength);

    /**
     * <p>Creates a span query where the resource property must match the given
     * value.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The name
     * @param value The value
     * @return The span query
     */
    CompassSpanQuery spanEq(String name, Object value);

    /**
     * <p>Creates a span query where the span occur within the first
     * <code>end</code> positions.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name  The name
     * @param value The value
     * @param end   The limit on the position from the start.
     * @return The span query
     */
    CompassSpanQuery spanFirst(String name, Object value, int end);

    /**
     * <p>Creates a span query.
     *
     * @param end The limit on the position from the start.
     * @return The span query
     */
    CompassSpanQuery spanFirst(CompassSpanQuery spanQuery, int end);

    /**
     * <p>Constructs a span near query builder.
     *
     * <p>The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param name The name
     * @return The span near query builder
     */
    CompassQuerySpanNearBuilder spanNear(String name);

    /**
     * <p>Creates a span query that excludes matches where one
     * {@link org.compass.core.CompassQuery.CompassSpanQuery} overlaps
     * with another.
     *
     * <p>Construct a span query matching spans from <code>include</code> which
     * have no overlap with spans from <code>exclude</code>.
     *
     * @param include The span query to include.
     * @param exclude The span query to exclude.
     * @return The span query
     */
    CompassSpanQuery spanNot(CompassSpanQuery include, CompassSpanQuery exclude);

    /**
     * Constructs a span or query builder.
     *
     * @return The span query builder
     */
    CompassQuerySpanOrBuilder spanOr();

    /**
     * Constructs a more like this query. The id can be an object of
     * the class (with the id attributes set), an array of id objects, or the
     * actual id object. Throws an exception if the resource is not found.
     */
    CompassMoreLikeThisQuery moreLikeThis(String alias, Serializable id);

    /**
     * Constructs a more like this query to find hits that are similar to
     * the give text represented by the reader.
     */
    CompassMoreLikeThisQuery moreLikeThis(Reader reader);
}
