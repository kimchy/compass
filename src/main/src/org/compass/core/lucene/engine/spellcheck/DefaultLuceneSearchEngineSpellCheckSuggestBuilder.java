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

package org.compass.core.lucene.engine.spellcheck;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.CompassSpellChecker;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckSuggestBuilder;
import org.compass.core.engine.spellcheck.SearchEngineSpellSuggestions;
import org.compass.core.mapping.ResourceMapping;

/**
 * @author kimchy
 */
public class DefaultLuceneSearchEngineSpellCheckSuggestBuilder implements SearchEngineSpellCheckSuggestBuilder {

    private InternalLuceneSearchEngineSpellCheckManager spellCheckerManager;

    private String word;

    private int numberOfSuggestions = 1;

    private String[] subIndexes;

    private String[] aliases;

    private String restrictToProperty;

    private boolean morePopular;

    private float accuracy = 0.5f;

    public DefaultLuceneSearchEngineSpellCheckSuggestBuilder(String word, InternalLuceneSearchEngineSpellCheckManager spellCheckerManager) {
        this.word = word;
        this.spellCheckerManager = spellCheckerManager;
        this.accuracy = spellCheckerManager.getDefaultAccuracy();
        this.numberOfSuggestions = spellCheckerManager.getDefaultNumberOfSuggestions();
    }

    public SearchEngineSpellCheckSuggestBuilder subIndexes(String... subIndexes) {
        this.subIndexes = subIndexes;
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder aliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder types(Class... types) {
        if (types == null) {
            this.aliases = null;
            return this; 
        }
        String[] aliases = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ResourceMapping resourceMapping = spellCheckerManager.getMapping().getRootMappingByClass(types[i]);
            aliases[i] = resourceMapping.getAlias();
        }
        aliases(aliases);
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder numberOfSuggestions(int numberOfSuggestions) {
        this.numberOfSuggestions = numberOfSuggestions;
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder restrictToProperty(String restrictToProperty) {
        this.restrictToProperty = restrictToProperty;
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder morePopular(boolean morePopular) {
        this.morePopular = morePopular;
        return this;
    }

    public SearchEngineSpellCheckSuggestBuilder accuracy(float accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public SearchEngineSpellSuggestions suggest() {
        return spellCheckerManager.execute(subIndexes, aliases, new SpellCheckerCallback<SearchEngineSpellSuggestions>() {
            public SearchEngineSpellSuggestions execute(CompassSpellChecker spellChecker, IndexReader reader) throws SearchEngineException {
                if (morePopular && restrictToProperty == null) {
                    restrictToProperty = spellCheckerManager.getDefaultProperty();
                }
                if (spellChecker == null || word == null || word.length() == 0) {
                    return new DefaultLuceneSearchEngineSpellSuggestions(false, new String[0]);
                }
                spellChecker.setAccuracy(accuracy);
                try {
                    if (restrictToProperty == null) {
                        reader = null;
                    }
                    String[] suggestions = spellChecker.suggestSimilar(word, numberOfSuggestions, reader, restrictToProperty, morePopular);
                    return new DefaultLuceneSearchEngineSpellSuggestions(spellChecker.exist(word), suggestions);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to suggest spell check", e);
                }
            }
        });
    }
}
