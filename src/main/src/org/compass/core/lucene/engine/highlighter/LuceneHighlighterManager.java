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

package org.compass.core.lucene.engine.highlighter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class LuceneHighlighterManager {

    private static final Log log = LogFactory.getLog(LuceneHighlighterManager.class);

    private LuceneHighlighterSettings defaultHighlighterSettings;

    private Map<String, LuceneHighlighterSettings> highlightersSettings = new HashMap<String, LuceneHighlighterSettings>();

    public void configure(CompassSettings settings) throws SearchEngineException {
        Map<String, CompassSettings> highlighterSettingGroups = settings.getSettingGroups(LuceneEnvironment.Highlighter.PREFIX);

        for (String highlighterName : highlighterSettingGroups.keySet()) {
            if (log.isInfoEnabled()) {
                log.info("Building highlighter [" + highlighterName + "]");
            }
            LuceneHighlighterSettings highlighter = buildHighlighter(highlighterName,
                    highlighterSettingGroups.get(highlighterName));
            highlightersSettings.put(highlighterName, highlighter);
        }
        defaultHighlighterSettings = highlightersSettings.get(LuceneEnvironment.Highlighter.DEFAULT_GROUP);
        if (defaultHighlighterSettings == null) {
            // if no default highlighter is defined, we need to configre one
            defaultHighlighterSettings = buildHighlighter(LuceneEnvironment.Highlighter.DEFAULT_GROUP,
                    new CompassSettings(settings.getClassLoader()));
            highlightersSettings.put(LuceneEnvironment.Highlighter.DEFAULT_GROUP, defaultHighlighterSettings);
        }
    }

    private LuceneHighlighterSettings buildHighlighter(String highlighterName, CompassSettings settings) {
        LuceneHighlighterFactory highlighterFactory = (LuceneHighlighterFactory) settings.getSettingAsInstance(LuceneEnvironment.Highlighter.FACTORY, DefaultLuceneHighlighterFactory.class.getName());
        return highlighterFactory.createHighlighterSettings(highlighterName, settings);
    }

    public LuceneHighlighterSettings getDefaultHighlighterSettings() {
        return defaultHighlighterSettings;
    }

    public LuceneHighlighterSettings getHighlighterSettings(String highlighterName) {
        return highlightersSettings.get(highlighterName);
    }

    public LuceneHighlighterSettings getHighlighterSettingsMustExists(String highlighterName) {
        LuceneHighlighterSettings highlighterSettings = highlightersSettings.get(highlighterName);
        if (highlighterSettings == null) {
            throw new SearchEngineException("No highlighter is defined for highlighter name [" + highlighterName + "]");
        }
        return highlighterSettings;
    }

}
