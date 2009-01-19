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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * 
 * @author kimchy
 * 
 */
public class SnowballAnalyzerBuilderDelegate implements AnalyzerBuilderDelegate {

    private static final Log log = LogFactory.getLog(SnowballAnalyzerBuilderDelegate.class);

    public Analyzer buildAnalyzer(String analyzerName, CompassSettings settings,
            DefaultLuceneAnalyzerFactory analyzerFactory) throws SearchEngineException {
        String snowballName = settings.getSetting(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
        if (snowballName == null) {
            throw new SearchEngineException("When using a snowball analyzer, must set the + ["
                    + LuceneEnvironment.Analyzer.Snowball.NAME_TYPE + "] setting for it");
        }
        if (log.isDebugEnabled()) {
            log.debug("Snowball Anayzer uses Name [" + snowballName + "]");
        }
        return new SnowballAnalyzer(snowballName, analyzerFactory.parseStopWords(analyzerName, settings, new String[0]));
    }
}
