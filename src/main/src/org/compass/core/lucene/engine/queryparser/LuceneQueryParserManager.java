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

package org.compass.core.lucene.engine.queryparser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.spellcheck.queryparser.SpellCheckLuceneQueryParser;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class LuceneQueryParserManager implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(LuceneQueryParserManager.class);

    private HashMap<String, LuceneQueryParser> queryParsers = new HashMap<String, LuceneQueryParser>();

    private LuceneSearchEngineFactory searchEngineFactory;

    public LuceneQueryParserManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        CompassSettings defaultGroupSettings = null;
        Map<String, CompassSettings> queryParserSettingGroups = settings.getSettingGroups(LuceneEnvironment.QueryParser.PREFIX);
        for (Map.Entry<String, CompassSettings> entry : queryParserSettingGroups.entrySet()) {
            String queryParserName = entry.getKey();
            CompassSettings queryParserSettings = entry.getValue();

            if (log.isDebugEnabled()) {
                log.debug("Building query parser [" + queryParserName + "] with settings " + queryParserSettings);
            }

            if (queryParserName.equals(LuceneEnvironment.QueryParser.DEFAULT_GROUP)) {
                defaultGroupSettings = queryParserSettings;
            }

            Object queryParserType = queryParserSettings.getSettingAsObject(LuceneEnvironment.QueryParser.TYPE);
            if (queryParserType == null) {
                if (queryParserName.equals(LuceneEnvironment.QueryParser.DEFAULT_GROUP)) {
                    // no problem, continue here and we will create the default one ourself using the provided settings
                    continue;
                }
                throw new ConfigurationException("Failed to locate query parser [" + queryParserName + "] type, it must be set");
            }
            LuceneQueryParser queryParser;
            if (queryParserType instanceof LuceneQueryParser) {
                queryParser = (LuceneQueryParser) queryParserType;
            } else {
                try {
                    queryParser = (LuceneQueryParser) ClassUtils.forName((String) queryParserType, settings.getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to create query parser class [" + queryParserType + "]", e);
                }
            }
            if (queryParser instanceof CompassConfigurable) {
                ((CompassConfigurable) queryParser).configure(queryParserSettings);
            }
            if (queryParser instanceof CompassMappingAware) {
                ((CompassMappingAware) queryParser).setCompassMapping(searchEngineFactory.getMapping());
            }
            if (queryParser instanceof SearchEngineFactoryAware) {
                ((SearchEngineFactoryAware) queryParser).setSearchEngineFactory(searchEngineFactory);
            }
            queryParsers.put(queryParserName, queryParser);
        }
        if (defaultGroupSettings == null) {
            defaultGroupSettings = new CompassSettings(settings.getClassLoader());
        }
        if (queryParsers.get(LuceneEnvironment.QueryParser.DEFAULT_GROUP) == null) {
            if (log.isDebugEnabled()) {
                log.debug("No default query parser found (under groupd [default]), registering a default one");
            }
            DefaultLuceneQueryParser queryParser = new DefaultLuceneQueryParser();
            queryParser.configure(defaultGroupSettings);
            queryParser.setCompassMapping(searchEngineFactory.getMapping());
            queryParser.setSearchEngineFactory(searchEngineFactory);
            queryParsers.put(LuceneEnvironment.QueryParser.DEFAULT_GROUP, queryParser);
        }
        if (searchEngineFactory.getSpellCheckManager() != null) {
            if (queryParsers.get(LuceneEnvironment.QueryParser.SPELLCHECK_GROUP) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No spellcheck query parser found (under groupd [spellcheck]), registering a default one");
                }
                SpellCheckLuceneQueryParser queryParser = new SpellCheckLuceneQueryParser();
                queryParser.configure(defaultGroupSettings);
                queryParser.setCompassMapping(searchEngineFactory.getMapping());
                queryParser.setSearchEngineFactory(searchEngineFactory);
                queryParsers.put(LuceneEnvironment.QueryParser.SPELLCHECK_GROUP, queryParser);
            }
        }
    }

    public LuceneQueryParser getDefaultQueryParser() {
        return getQueryParser(LuceneEnvironment.QueryParser.DEFAULT_GROUP);
    }

    public LuceneQueryParser getQueryParser(String queryParserName) throws IllegalArgumentException {
        LuceneQueryParser queryParser = queryParsers.get(queryParserName);
        if (queryParser == null) {
            throw new IllegalArgumentException("No query parser is configured under [" + queryParserName + "]");
        }
        return queryParser;
    }
}
