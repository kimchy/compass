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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.CompassSpellChecker;
import org.compass.core.engine.SearchEngineException;

/**
 * A callback to perform {@link org.apache.lucene.search.spell.CompassSpellChecker} operations with.
 *
 * @author kimchy
 */
public interface SpellCheckerCallback<T> {

    /**
     * A callback to execute speckk check operations.
     *
     * <p>Note, this callback might have the spellChecker as <code>null</code>. This means there is no
     * spell index yet present.
     */
    T execute(CompassSpellChecker spellChecker, IndexReader reader) throws SearchEngineException;
}
