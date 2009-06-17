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

import java.util.Locale;

/**
 * An object representing a Compass query. The query is created using the
 * {@link org.compass.core.CompassQueryBuilder}, and used to get the
 * matching {@link org.compass.core.CompassHits}.
 * <p>
 * The query be sorted as well using one of the <code>addSort</code>
 * operations. Note that adding sorting is only applicable for the query that
 * will execute the {@link #hits()} operation.
 *
 * @author kimchy
 */
public interface CompassQuery {

    /**
     * An extension to the {@link CompassQuery} interface. Handles special span
     * queries.
     *
     * @author kimchy
     */
    public static interface CompassSpanQuery extends CompassQuery {

    }

    /**
     * Used to set the sort direction for the query.
     *
     * @author kimchy
     */
    public static enum SortDirection {

        /**
         * The default sort direction, which is descending for relevance type
         * and increasing for all the rest.
         */
        AUTO,

        /**
         * The reverse order of the <code>AUTO</code> order.
         */
        REVERSE

    }

    /**
     * Used to define the type of the property that will be sorted. Note that
     * <code>INT</code> has the lowest memorty requirements and
     * <code>STRING</code> the most.
     *
     * @author kimchy
     */
    public static enum SortPropertyType {

        /**
         * Guess type of sort based on proeprty contents. A regular expression
         * is used to look at the first term indexed for the proeprty and
         * determine if it represents an integer number, a floating point
         * number, or just arbitrary string characters.
         */
        AUTO,

        /**
         * Sort using term values as Strings. Sort values are String and lower
         * values are at the front.
         */
        STRING,

        /**
         * Sort using term values as encoded bytes.  Sort values are bytes and lower values are at the front
         */
        BYTE,

        /**
         * Sort using term values as encoded Integers. Sort values are Integer
         * and lower values are at the front.
         */
        INT,

        /**
         * Sort using term values as encoded Longs.  Sort values are Long and
         * lower values are at the front.
         */
        LONG,

        /**
         * Sort using term values as encoded Floats. Sort values are Float and
         * lower values are at the front.
         */
        FLOAT,

        /**
         * Sort using term values as encoded Doubles.  Sort values are Double and
         * lower values are at the front.
         */
        DOUBLE
    }

    /**
     * Implicit types that the hits can be sorted by.
     *
     * @author kimchy
     */
    public static enum SortImplicitType {

        /**
         * Sort by resource score (relevancy). Sort values are Float and higher
         * values are at the front.
         */
        SCORE,

        /**
         * Sort by document number (index order). Sort values are Integer and
         * lower values are at the front. Note, that if an updated occurs, the
         * document number will be higher.
         */
        DOC
    }

    /**
     * Attaches the Compass Query to the current session. Should be called when the query
     * is created using {@link org.compass.core.Compass#queryBuilder()}.
     */
    CompassQuery attach(CompassSession session);

    /**
     * Attaches the Compass Query to the current session. Should be called when the query
     * is created using {@link org.compass.core.Compass#queryBuilder()}.
     */
    CompassQuery attach(CompassSearchSession session);

    /**
     * Detaches the query from the current session. Note, not required to be called since
     * it will be automatically detached when the session is closed.
     */
    void detach();

    /**
     * Sets the boost for this query to <code>boost</code>. Hits matching
     * this query will (in addition to the normal weightings) have their score
     * multiplied by <code>boost</code>.
     */
    CompassQuery setBoost(float boost);

    /**
     * Adds sorting on the given property name. The type of sorting will be
     * identified automatically (though it is preferable to specify it in
     * advance using the {@link #addSort(String,SortPropertyType)}.
     * <p>
     * Note that the property must be <code>UN_TOKENIZED</code>, and stored.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param propertyName The property name that will be sorted.
     */
    CompassQuery addSort(String propertyName);

    /**
     * Adds sorting on the given property name. The type of sorting will be
     * identified automatically (though it is preferable to specify it in
     * advance using the {@link #addSort(String,SortPropertyType)}.
     * <p>
     * Note that the property must be <code>UN_TOKENIZED</code>, and stored.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param propertyName The property name that will be sorted.
     * @param direction    The direction for the sorting.
     * @return the query
     */
    CompassQuery addSort(String propertyName, SortDirection direction);

    /**
     * Adds sorting on the given property name, and using the given property
     * type. Note that <code>INT</code> has the lowest memorty requirements
     * and <code>STRING</code> the most.
     * <p>
     * Note that the property must be <code>UN_TOKENIZED</code>, and stored.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param propertyName The property name that will be sorted.
     * @param type         The type of the propert.
     * @return the query
     */
    CompassQuery addSort(String propertyName, SortPropertyType type);

    /**
     * Adds sorting on the given property name, and using the given property
     * type. Note that <code>INT</code> has the lowest memorty requirements
     * and <code>STRING</code> the most.
     * <p>
     * Note that the property must be <code>UN_TOKENIZED</code>, and stored.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param propertyName The property name that will be sorted.
     * @param type         The type of the propert.
     * @param direction    The direction of the sorting.
     * @return the query
     */
    CompassQuery addSort(String propertyName, SortPropertyType type, SortDirection direction);

    /**
     * Adds sorting on implicit types, which are not direct properties values.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param implicitType The implicit type to add sorting on.
     */
    CompassQuery addSort(SortImplicitType implicitType);

    /**
     * Adds sorting on implicit types, which are not direct properties values.
     * <p>
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param implicitType The implicit type to add sorting on.
     * @param direction    The direction of the sorting.
     */
    CompassQuery addSort(SortImplicitType implicitType, SortDirection direction);

    /**
     * Adds sorting on the given property name, and using the given locale.
     *
     * Note, that the sort option will only work on the outer most query (i.e.
     * the one that the <code>hits</code> is called on).
     *
     * @param propertyName The property name that will be sorted.
     * @param locale       The locale.
     * @param direction    The direction of the sorting.
     * @return the query
     */
    CompassQuery addSort(String propertyName, Locale locale, SortDirection direction);

    /**
     * Adds sorting on the given property name, and using the given locale.
     *
     * @param propertyName The property name that will be sorted.
     * @param locale       The locale.
     * @return the query
     */
    CompassQuery addSort(String propertyName, Locale locale);

    /**
     * Narrows down the query to be executed only against the given sub indexes.
     * If set to <code>null</code>, will use all sub indexes.
     *
     * @param subIndexes sub indexes the query will be executed against
     * @return The query
     */
    CompassQuery setSubIndexes(String... subIndexes);

    /**
     * Narrows down the query to be executed only against the given aliases.
     * If set to <code>null</code>, will use all aliases.
     *
     * @param aliases aliases the query will be executed against
     * @return th query
     */
    CompassQuery setAliases(String... aliases);

    /**
     * Narrows down the query to be executed only against the given types.
     * Internally will translate the classes to the relevant <b>root</b>
     * aliases and call {@link #setAliases(String[])}.
     *
     * @param types class types the query will be executed against
     * @return the query
     */
    CompassQuery setTypes(Class... types);

    /**
     * Sets a filter to the query. Please see {@link CompassQueryFilterBuilder}.
     *
     * @param filter The filter for the query
     * @return the query
     */
    CompassQuery setFilter(CompassQueryFilter filter);

    /**
     * Returns the filter set using {@link #setFilter(CompassQueryFilter)}. <code>null</code>
     * if none is set.
     *
     * @return The filter set using {@link #setFilter(CompassQueryFilter)}
     */
    CompassQueryFilter getFilter();

    /**
     * Causes the query to be rewritten before executed to search.
     */
    CompassQuery rewrite();

    /**
     * Returns the suggested query (based on spell check). If spell check is disabled
     * the same query is returned.
     *
     * <p>In order to know if the query was actually replaced with a suggested one, call
     * {@link CompassQuery#isSuggested()}.
     */
    CompassQuery getSuggestedQuery();

    /**
     * Returns <code>true</code> if this is a suggested query. For example, when performing
     * a search query with spell check enabled, then it will return <code>true</code> if the
     * query was modified to correct some spelling mistakes.
     */
    boolean isSuggested();

    /**
     * Performs the search and returns the hits that match the qeury.
     */
    CompassHits hits() throws CompassException;

    /**
     * Deletes all the matching hits from the index.
     */
    void delete();

    /**
     * Returns the count of hits that match this query. Note, this will be faster than
     * {@link CompassHitsOperations#length()}.
     *
     * <p>Same as calling <code>count(0.0f)</code>.
     */
    long count();

    /**
     * Returns the count of hits that match this query and are higher than the given score.
     * Note, this will be faster than {@link CompassHitsOperations#length()}.
     */
    long count(float minimumScore);

    /**
     * Clones the given query.
     */
    Object clone() throws CloneNotSupportedException;
}
