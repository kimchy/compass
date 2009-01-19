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

package org.compass.core.lucene.engine.analyzer.synonym;

import org.compass.core.config.CompassConfigurable;

/**
 * An implementation for a synonym lookup. Provides a list of synonyms
 * for a given value.
 * <p/>
 * Most of the time, the synonym data will be lookup up during the configuration
 * process ({@link #configure(org.compass.core.config.CompassSettings)}, using the
 * dedicated settings that will "point" to the location of the synonym data
 * (i.e. a simple file path location for synonyms).
 *
 * @author kimchy
 */
public interface SynonymLookupProvider extends CompassConfigurable {

    /**
     * Returns a list of synonyms for the given value.
     */
    String[] lookupSynonyms(String value);
}
