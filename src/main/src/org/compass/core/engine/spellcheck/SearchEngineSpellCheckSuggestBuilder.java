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

package org.compass.core.engine.spellcheck;

/**
 * A builder allowing to create suggestions for a given word.
 *
 * @author kimchy
 */
public interface SearchEngineSpellCheckSuggestBuilder {

    /**
     * Narrows down the spell check suggestions to the given sub indexes.
     */
    SearchEngineSpellCheckSuggestBuilder subIndexes(String... subIndexes);

    /**
     * Narrows down the spell check suggestions to the given sub aliases.
     */
    SearchEngineSpellCheckSuggestBuilder aliases(String... aliases);

    /**
     * Narrows down the spell check suggestions to the given types.
     */
    SearchEngineSpellCheckSuggestBuilder types(Class ... types);

    /**
     * Restricts the number of suggestions. Defaults to <code>1</code>.
     */
    SearchEngineSpellCheckSuggestBuilder numberOfSuggestions(int numberOfSuggestions);

    /**
     * Restricts the suggested words to the words present in this property.
     */
    SearchEngineSpellCheckSuggestBuilder restrictToProperty(String restrictToProperty);

    /**
     * Return only the suggest words that are more frequent than the searched word.
     */
    SearchEngineSpellCheckSuggestBuilder morePopular(boolean morePopular);

    /**
     * Sets the accuracy. Defauts to the value of {@link org.compass.core.lucene.LuceneEnvironment.SpellCheck#ACCURACY}
     * which, in turn, defaults to <code>0.5f</code>.
     */
    SearchEngineSpellCheckSuggestBuilder accuracy(float accuracy);

    /**
     * Returns the given suggestions for the word.
     */
    SearchEngineSpellSuggestions suggest();
}
