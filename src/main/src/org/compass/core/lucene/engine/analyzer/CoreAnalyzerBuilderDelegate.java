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
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * 
 * @author kimchy
 * 
 */
public class CoreAnalyzerBuilderDelegate implements AnalyzerBuilderDelegate {

    public Analyzer buildAnalyzer(String analyzerName, CompassSettings settings,
            DefaultLuceneAnalyzerFactory analyzerFactory) throws SearchEngineException {

        String analyzerSetting = settings.getSetting(LuceneEnvironment.Analyzer.TYPE,
                LuceneEnvironment.Analyzer.CoreTypes.STANDARD);

        Analyzer analyzer = null;

        if (LuceneEnvironment.Analyzer.CoreTypes.WHITESPACE.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new WhitespaceAnalyzer();
        } else if (LuceneEnvironment.Analyzer.CoreTypes.STANDARD.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new StandardAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    StandardAnalyzer.STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.CoreTypes.SIMPLE.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new SimpleAnalyzer();
        } else if (LuceneEnvironment.Analyzer.CoreTypes.STOP.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new StopAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    StopAnalyzer.ENGLISH_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.CoreTypes.KEYWORD.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new KeywordAnalyzer();
        }
        
        return analyzer;
    }
}
