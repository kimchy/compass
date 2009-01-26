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

package org.compass.core.engine;

import java.util.Locale;

import org.compass.core.CompassQuery.SortDirection;
import org.compass.core.CompassQuery.SortImplicitType;
import org.compass.core.CompassQuery.SortPropertyType;

/**
 * @author kimchy
 */
public interface SearchEngineQuery {

    public static interface SearchEngineSpanQuery extends SearchEngineQuery {

    }

    SearchEngineQuery setBoost(float boost);

    SearchEngineQuery addSort(String propertyName);

    SearchEngineQuery addSort(String propertyName, SortDirection direction);

    SearchEngineQuery addSort(String propertyName, SortPropertyType type);

    SearchEngineQuery addSort(String propertyName, SortPropertyType type, SortDirection direction);

    SearchEngineQuery addSort(SortImplicitType implicitType);

    SearchEngineQuery addSort(SortImplicitType implicitType, SortDirection direction);

    SearchEngineQuery addSort(String propertyName, Locale locale, SortDirection direction);

    SearchEngineQuery addSort(String propertyName, Locale locale);

    SearchEngineQuery setSubIndexes(String[] subindexes);

    SearchEngineQuery setAliases(String[] aliases);

    SearchEngineQuery setFilter(SearchEngineQueryFilter filter);

    SearchEngineQuery rewrite();

    boolean isSuggested();

    SearchEngineHits hits(SearchEngine searchEngine) throws SearchEngineException;

    long count(SearchEngine searchEngine);

    long count(SearchEngine searchEngine, float minimumScore);

    Object clone() throws CloneNotSupportedException;
}
