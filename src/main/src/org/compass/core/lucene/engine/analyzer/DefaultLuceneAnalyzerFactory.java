/*
 * Copyright 2004-2006 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class DefaultLuceneAnalyzerFactory implements LuceneAnalyzerFactory {

    private static final Log log = LogFactory.getLog(DefaultLuceneAnalyzerFactory.class);

    private static final Map extednedAnalyzers;

    private static final Map coreAnalyzers;

    static {
        coreAnalyzers = new HashMap();
        coreAnalyzers.put(LuceneEnvironment.Analyzer.CoreTypes.WHITESPACE, "");
        coreAnalyzers.put(LuceneEnvironment.Analyzer.CoreTypes.STANDARD, "");
        coreAnalyzers.put(LuceneEnvironment.Analyzer.CoreTypes.SIMPLE, "");
        coreAnalyzers.put(LuceneEnvironment.Analyzer.CoreTypes.STOP, "");

        extednedAnalyzers = new HashMap();
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.BRAZILIAN, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.CJK, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.CHINESE, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.CZECH, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.GERMAN, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.GREEK, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.FRENCH, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.DUTCH, "");
        extednedAnalyzers.put(LuceneEnvironment.Analyzer.ExtendedTypes.RUSSIAN, "");
    }

    public Analyzer createAnalyzer(String analyzerName, CompassSettings settings) throws SearchEngineException {
        Analyzer analyzer;
        String analyzerSetting = settings.getSetting(LuceneEnvironment.Analyzer.TYPE,
                LuceneEnvironment.Analyzer.CoreTypes.STANDARD);
        if (log.isDebugEnabled()) {
            log.debug("Analyzer [" + analyzerName + "] uses Lucene analyzer [" + analyzerSetting + "]");
        }
        if (coreAnalyzers.get(analyzerSetting.toLowerCase()) != null) {
            AnalyzerBuilderDelegate analyzerBuilderDelegate = new CoreAnalyzerBuilderDelegate();
            analyzer = analyzerBuilderDelegate.buildAnalyzer(analyzerName, settings, this);
        } else if (LuceneEnvironment.Analyzer.Snowball.SNOWBALL.equalsIgnoreCase(analyzerSetting)) {
            AnalyzerBuilderDelegate analyzerBuilderDelegate = new SnowballAnalyzerBuilderDelegate();
            analyzer = analyzerBuilderDelegate.buildAnalyzer(analyzerName, settings, this);
        } else if (extednedAnalyzers.get(analyzerSetting.toLowerCase()) != null) {
            AnalyzerBuilderDelegate analyzerBuilderDelegate = new ExtendedAnalyzerBuilderDelegate();
            analyzer = analyzerBuilderDelegate.buildAnalyzer(analyzerName, settings, this);
        } else {
            // the analyzer must be a fully qualified class, try to instansiate
            try {
                analyzer = (Analyzer) ClassUtils.forName(analyzerSetting).newInstance();
            } catch (Exception e) {
                throw new SearchEngineException("Cannot instantiate Lucene Analyzer [" + analyzerSetting
                        + "] for analyzer [" + analyzerName + "]. Please verify the analyzer setting at ["
                        + LuceneEnvironment.Analyzer.TYPE + "]", e);
            }
            if (analyzer instanceof CompassConfigurable) {
                ((CompassConfigurable) analyzer).configure(settings);
            }
        }
        return analyzer;
    }

    public String[] parseStopWords(String analyzerName, CompassSettings settings, String[] defaultStopWords) {
        String stopWords = settings.getSetting(LuceneEnvironment.Analyzer.STOPWORDS);
        if (stopWords == null) {
            if (log.isTraceEnabled()) {
                log.trace("Anayzer [" + analyzerName + "] uses default stop words ["
                        + StringUtils.arrayToCommaDelimitedString(defaultStopWords) + "]");
            }
            return defaultStopWords;
        }
        boolean addStopWords = false;
        if (stopWords.startsWith("+")) {
            addStopWords = true;
            stopWords = stopWords.substring(1);
        }
        StringTokenizer st = new StringTokenizer(stopWords, ",");
        ArrayList listStopWords = new ArrayList();
        while (st.hasMoreTokens()) {
            String stopword = st.nextToken().trim();
            if (StringUtils.hasLength(stopword)) {
                listStopWords.add(stopword);
            }
        }
        String[] arrStopWords = (String[]) listStopWords.toArray(new String[listStopWords.size()]);
        
        if (addStopWords) {
            if (log.isTraceEnabled()) {
                log.trace("Analyzer [" + analyzerName + "] uses default stop words ["
                        + StringUtils.arrayToCommaDelimitedString(defaultStopWords) + "]");
                log.trace("Analyzer [" + analyzerName + "] and uses user stop words ["
                        + StringUtils.arrayToCommaDelimitedString(arrStopWords) + "]");
            }
            String[] tempStopWords = arrStopWords;
            arrStopWords = new String[tempStopWords.length + defaultStopWords.length];
            System.arraycopy(defaultStopWords, 0, arrStopWords, 0, defaultStopWords.length);
            System.arraycopy(tempStopWords, 0, arrStopWords, defaultStopWords.length, tempStopWords.length);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Analyzer [" + analyzerName + "] uses user stop words ["
                        + StringUtils.arrayToCommaDelimitedString(arrStopWords) + "]");
            }
        }
        return arrStopWords;
    }
}
