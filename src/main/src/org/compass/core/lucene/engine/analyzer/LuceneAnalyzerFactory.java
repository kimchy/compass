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

package org.compass.core.lucene.engine.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;

/**
 * A factory for Lucene {@link org.apache.lucene.analysis.Analyzer}.
 *
 * @author kimchy
 */
public interface LuceneAnalyzerFactory {

    /**
     * Creates a thread safe analyzer instance to be used. The factory is given the analyzer name and the settings
     * that are relevant for that analyzer.
     *
     * <p>For example, setting <code>compass.engine.analyzer.[analyzer name].factory</code> with the factory class
     * name (or the actual instance of the factory) will use the factory to create the actual instance of the
     * analyzer.
     *
     * <p>The settings are the one bounded to the specific analyzer. For examle, a setting under the key:
     * <code>compass.engine.analyzer.[analyzer name].key1=prop1</code> will be injected as <code>key1=prop1</code>.
     * The global settings can still be accessed using {@link org.compass.core.config.CompassSettings#getGloablSettings()}.
     *
     * @param analyzerName The analyzer name
     * @param settings     The settings boudned to the specific analyzer
     * @return An instnace of Lucene Analyzer
     * @throws SearchEngineException
     */
    public Analyzer createAnalyzer(String analyzerName, CompassSettings settings) throws SearchEngineException;
}
