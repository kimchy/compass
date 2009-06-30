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

import org.apache.lucene.search.spell.CompassSpellChecker;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.spellcheck.spi.InternalSearchEngineSpellCheckManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.mapping.CompassMapping;

/**
 * @author kimchy
 */
public interface InternalLuceneSearchEngineSpellCheckManager extends InternalSearchEngineSpellCheckManager {

    void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping);

    LuceneSearchEngineStore getStore();

    String getStoreSubContext();

    String getDefaultProperty();

    float getDefaultAccuracy();

    int getDefaultNumberOfSuggestions();

    CompassMapping getMapping();

    CompassSpellChecker createSpellChecker(final String[] subIndexes, final String[] aliases);

    <T> T execute(final String[] subIndexes, final String[] aliases, final SpellCheckerCallback<T> callback);
}
