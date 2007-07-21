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

package org.compass.core.lucene.engine.queryparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class LuceneQueryParserManager implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(LuceneQueryParserManager.class);

    private HashMap queryParsers = new HashMap();

    private LuceneSearchEngineFactory searchEngineFactory;

    public LuceneQueryParserManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        Map queryParserSettingGroups = settings.getSettingGroups(LuceneEnvironment.QueryParser.PREFIX);
        for (Iterator it = queryParserSettingGroups.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String queryParserName = (String) entry.getKey();
            CompassSettings queryParserSettings = (CompassSettings) entry.getValue();

            if (log.isInfoEnabled()) {
                log.info("Building query parser [" + queryParserName + "]");
            }
            String queryParserType = queryParserSettings.getSetting(LuceneEnvironment.QueryParser.TYPE);
            if (queryParserType == null) {
                throw new ConfigurationException("Failed to locate query parser [" + queryParserName + "] type, it must be set");
            }
            LuceneQueryParser queryParser;
            try {
                queryParser = (LuceneQueryParser) ClassUtils.forName(queryParserType).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to create query parser class [" + queryParserType + "]", e);
            }
            if (queryParser instanceof CompassConfigurable) {
                ((CompassConfigurable) queryParser).configure(queryParserSettings);
            }
            if (queryParser instanceof CompassMappingAware) {
                ((CompassMappingAware) queryParser).setCompassMapping(searchEngineFactory.getMapping());
            }
            queryParsers.put(queryParserName, queryParser);
        }
        if (queryParsers.get(LuceneEnvironment.QueryParser.DEFAULT_GROUP) == null) {
            DefaultLuceneQueryParser queryParser = new DefaultLuceneQueryParser();
            queryParser.setCompassMapping(searchEngineFactory.getMapping());
            queryParsers.put(LuceneEnvironment.QueryParser.DEFAULT_GROUP, queryParser);
        }
    }

    public LuceneQueryParser getDefaultQueryParser() {
        return getQueryParser(LuceneEnvironment.QueryParser.DEFAULT_GROUP);
    }

    public LuceneQueryParser getQueryParser(String queryParserName) throws IllegalArgumentException {
        LuceneQueryParser queryParser = (LuceneQueryParser) queryParsers.get(queryParserName);
        if (queryParser == null) {
            throw new IllegalArgumentException("No query parser is configured under [" + queryParserName + "]");
        }
        return queryParser;
    }
}
