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
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * 
 * @author kimchy
 * 
 */
public class ExtendedAnalyzerBuilderDelegate implements AnalyzerBuilderDelegate {

    public Analyzer buildAnalyzer(String analyzerName, CompassSettings settings,
            DefaultLuceneAnalyzerFactory analyzerFactory) throws SearchEngineException {

        String analyzerSetting = settings.getSetting(LuceneEnvironment.Analyzer.TYPE,
                LuceneEnvironment.Analyzer.CoreTypes.STANDARD);

        Analyzer analyzer = null;
        if (LuceneEnvironment.Analyzer.ExtendedTypes.BRAZILIAN.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new BrazilianAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    BrazilianAnalyzer.BRAZILIAN_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.CJK.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new CJKAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings, CJKAnalyzer.STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.CHINESE.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new ChineseAnalyzer();
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.CZECH.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new CzechAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    CzechAnalyzer.CZECH_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.GERMAN.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new GermanAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    GermanAnalyzer.GERMAN_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.GREEK.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new GreekAnalyzer();
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.FRENCH.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new FrenchAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    FrenchAnalyzer.FRENCH_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.DUTCH.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new DutchAnalyzer(analyzerFactory.parseStopWords(analyzerName, settings,
                    DutchAnalyzer.DUTCH_STOP_WORDS));
        } else if (LuceneEnvironment.Analyzer.ExtendedTypes.RUSSIAN.equalsIgnoreCase(analyzerSetting)) {
            analyzer = new RussianAnalyzer();
        }

        return analyzer;
    }
}
