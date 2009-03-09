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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.analyzer.synonym.SynonymAnalyzerTokenFilterProvider;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceAnalyzerController;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * Manages all the configured Lucene analyzers within Compass.
 *
 * @author kimchy
 */
public class LuceneAnalyzerManager {

    private static final Log log = LogFactory.getLog(LuceneAnalyzerManager.class);

    private final HashMap<String, Analyzer> analyzers = new HashMap<String, Analyzer>();

    private Analyzer defaultAnalyzer;

    private Analyzer searchAnalyzer;

    private final HashMap<String, Analyzer> aliasAnalyzers = new HashMap<String, Analyzer>();

    private final HashMap<String, LuceneAnalyzerTokenFilterProvider> analyzersFilters = new HashMap<String, LuceneAnalyzerTokenFilterProvider>();

    private final CompassMapping mapping;

    public LuceneAnalyzerManager(CompassSettings settings, CompassMapping mapping)
            throws SearchEngineException {
        checkNotUsingOldVersionsAnalyzerSettings(settings);
        this.mapping = mapping;
        buildAnalyzersFilters(settings);
        buildAnalyzers(settings, mapping);
    }

    public void close() {
        for (Analyzer analyzer : analyzers.values()) {
            try {
                analyzer.close();
            } catch (Exception e) {
                // ignore
            }
        }
        try {
            defaultAnalyzer.close();
        } catch (Exception e) {
            // ignore
        }
        try {
            searchAnalyzer.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private void buildAnalyzersFilters(CompassSettings settings) {
        Map<String, CompassSettings> analyzerFilterSettingGroups = settings.getSettingGroups(LuceneEnvironment.AnalyzerFilter.PREFIX);
        for (String analyzerFilterName : analyzerFilterSettingGroups.keySet()) {
            if (log.isInfoEnabled()) {
                log.info("Building analyzer filter [" + analyzerFilterName + "]");
            }
            CompassSettings analyzerFilterSettings = analyzerFilterSettingGroups.get(analyzerFilterName);
            LuceneAnalyzerTokenFilterProvider provider;

            Object obj = analyzerFilterSettings.getSettingAsObject(LuceneEnvironment.AnalyzerFilter.TYPE);
            if (obj instanceof LuceneAnalyzerTokenFilterProvider) {
                provider = (LuceneAnalyzerTokenFilterProvider) obj;
            } else {
                String analyzerFilterType = analyzerFilterSettings.getSetting(LuceneEnvironment.AnalyzerFilter.TYPE);
                if (analyzerFilterType == null) {
                    throw new SearchEngineException("Failed to locate analyzer filter [" + analyzerFilterName + "] type, it must be set");
                }
                try {
                    if (analyzerFilterType.equals(LuceneEnvironment.AnalyzerFilter.SYNONYM_TYPE)) {
                        analyzerFilterType = SynonymAnalyzerTokenFilterProvider.class.getName();
                    }
                    provider = (LuceneAnalyzerTokenFilterProvider) ClassUtils.forName(analyzerFilterType, settings.getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to create analyzer filter [" + analyzerFilterName + "]", e);
                }
            }
            provider.configure(analyzerFilterSettings);
            analyzersFilters.put(analyzerFilterName, provider);
        }
    }

    private void buildAnalyzers(CompassSettings settings, CompassMapping mapping) {
        Map<String, CompassSettings> analyzerSettingGroups = settings.getSettingGroups(LuceneEnvironment.Analyzer.PREFIX);
        for (String analyzerName : analyzerSettingGroups.keySet()) {
            if (log.isInfoEnabled()) {
                log.info("Building analyzer [" + analyzerName + "]");
            }
            Analyzer analyzer = buildAnalyzer(analyzerName, analyzerSettingGroups.get(analyzerName));
            analyzers.put(analyzerName, analyzer);
        }
        defaultAnalyzer = analyzers.get(LuceneEnvironment.Analyzer.DEFAULT_GROUP);
        if (defaultAnalyzer == null) {
            // if no default anayzer is defined, we need to configre one
            defaultAnalyzer = buildAnalyzer(LuceneEnvironment.Analyzer.DEFAULT_GROUP, new CompassSettings(settings.getClassLoader()));
            analyzers.put(LuceneEnvironment.Analyzer.DEFAULT_GROUP, defaultAnalyzer);
        }
        searchAnalyzer = analyzers.get(LuceneEnvironment.Analyzer.SEARCH_GROUP);
        if (searchAnalyzer == null) {
            searchAnalyzer = defaultAnalyzer;
        }
        // build the analyzers for the different resources
        buildAnalyzerPerAlias(mapping);
    }

    private void buildAnalyzerPerAlias(CompassMapping mapping)
            throws SearchEngineException {
        for (ResourceMapping resourceMapping : mapping.getRootMappings()) {
            String alias = resourceMapping.getAlias();
            String resourceAnalyzerName = LuceneEnvironment.Analyzer.DEFAULT_GROUP;
            if (resourceMapping.getAnalyzer() != null) {
                resourceAnalyzerName = resourceMapping.getAnalyzer();
            }
            Analyzer resourceAnalyzer = buildAnalyzerPerResourcePropertyIfNeeded(resourceMapping, resourceAnalyzerName);
            aliasAnalyzers.put(alias, resourceAnalyzer);
        }
    }

    /**
     * Returns the default Lucene {@link Analyzer} for Compass.
     */
    public Analyzer getDefaultAnalyzer() {
        return defaultAnalyzer;
    }

    /**
     * Returns the search Lucene {@link Analyzer}.
     */
    public Analyzer getSearchAnalyzer() {
        return searchAnalyzer;
    }

    /**
     * Returns the Lucene {@link Analyzer} registed under the given name.
     */
    public Analyzer getAnalyzer(String analyzerName) {
        return analyzers.get(analyzerName);
    }

    /**
     * Returns the Lucene {@link Analyzer} for the given alias. Might build a per field analyzer
     * if the resource has more than one analyzer against one of its properties.
     */
    public Analyzer getAnalyzerByAlias(String alias) {
        return aliasAnalyzers.get(alias);
    }

    public Analyzer getAnalyzerByAliasMustExists(String alias) throws SearchEngineException {
        Analyzer analyzer = aliasAnalyzers.get(alias);
        if (analyzer == null) {
            throw new SearchEngineException("No analyzer is defined for alias [" + alias + "]");
        }
        return analyzer;
    }

    /**
     * Returns the Lucene {@link Analyzer} based on the give {@link Resource}. Will build a specifc
     * per field analyzr if the given {@link Resource} has properties with different analyzers.
     * Will also take into account if the resource has an analyzer controller based on the analyzer
     * controller property value.
     */
    public Analyzer getAnalyzerByResource(Resource resource) throws SearchEngineException {
        String alias = resource.getAlias();
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        if (resourceMapping.getAnalyzerController() == null) {
            return aliasAnalyzers.get(alias);
        }
        ResourceAnalyzerController analyzerController = resourceMapping.getAnalyzerController();
        String analyzerPropertyName = analyzerController.getAnalyzerResourcePropertyName();
        String analyzerName = resource.getValue(analyzerPropertyName);
        if (analyzerName == null) {
            analyzerName = analyzerController.getNullAnalyzer();
        }
        return buildAnalyzerPerResourcePropertyIfNeeded(resourceMapping, analyzerName);
    }

    public Analyzer getAnalyzerMustExist(String analyzerName) throws SearchEngineException {
        Analyzer analyzer = analyzers.get(analyzerName);
        if (analyzer == null) {
            throw new SearchEngineException("No analyzer is defined for analyzer name [" + analyzerName + "]");
        }
        return analyzer;
    }

    private Analyzer buildAnalyzer(String analyzerName, CompassSettings settings) {
        LuceneAnalyzerFactory analyzerFactory = (LuceneAnalyzerFactory) settings.getSettingAsInstance(LuceneEnvironment.Analyzer.FACTORY, DefaultLuceneAnalyzerFactory.class.getName());
        Analyzer analyzer = analyzerFactory.createAnalyzer(analyzerName, settings);
        String filters = settings.getSetting(LuceneEnvironment.Analyzer.FILTERS);
        if (filters != null) {
            StringTokenizer tokenizer = new StringTokenizer(filters, ",");
            ArrayList<LuceneAnalyzerTokenFilterProvider> filterProviders = new ArrayList<LuceneAnalyzerTokenFilterProvider>();
            while (tokenizer.hasMoreTokens()) {
                String filterProviderLookupName = tokenizer.nextToken();
                if (!StringUtils.hasText(filterProviderLookupName)) {
                    continue;
                }
                LuceneAnalyzerTokenFilterProvider provider = analyzersFilters.get(filterProviderLookupName);
                if (provider == null) {
                    throw new SearchEngineException("Failed to located filter provider [" + filterProviderLookupName
                            + "] for analyzer [" + analyzerName + "]");
                }
                filterProviders.add(provider);
            }
            analyzer = new LuceneAnalyzerFilterWrapper(analyzer,
                    filterProviders.toArray(new LuceneAnalyzerTokenFilterProvider[filterProviders.size()]));
        }
        return analyzer;
    }

    private Analyzer buildAnalyzerPerResourcePropertyIfNeeded(ResourceMapping resourceMapping,
                                                              String resourceAnalyzerName) {
        Analyzer resourceAnalyzer = getAnalyzerMustExist(resourceAnalyzerName);
        // create the per field analyzer only if there is one that is
        // specific to a resource property or
        if (resourceMapping.hasSpecificAnalyzerPerResourceProperty()) {
            PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(resourceAnalyzer);
            ResourcePropertyMapping[] propertyMappings = resourceMapping.getResourcePropertyMappings();
            for (ResourcePropertyMapping propertyMapping : propertyMappings) {
                if (propertyMapping.getAnalyzer() != null) {
                    Analyzer propertyAnalyzer = getAnalyzer(propertyMapping.getAnalyzer());
                    if (propertyAnalyzer == null) {
                        throw new SearchEngineException("Failed to find analyzer [" + propertyMapping.getAnalyzer()
                                + "] for alias [" + resourceMapping.getAlias() + "] and property ["
                                + propertyMapping.getName() + "]");
                    }
                    perFieldAnalyzerWrapper.addAnalyzer(propertyMapping.getPath().getPath(), propertyAnalyzer);
                }
            }
            return perFieldAnalyzerWrapper;
        }
        return resourceAnalyzer;
    }

    private void checkNotUsingOldVersionsAnalyzerSettings(CompassSettings settings) throws SearchEngineException {
        // just so upgrades will be simpler
        if (settings.getSetting("compass.engine.analyzer.factory") != null) {
            throw new ConfigurationException(
                    "Old analyzer setting for analyzer factory, use [compass.engine.analyzer.default.*] instead");
        }
        if (settings.getSetting("compass.engine.analyzer") != null) {
            throw new ConfigurationException(
                    "Old analyzer setting for analyzer, use [compass.engine.analyzer.default.*] instead");
        }
        if (settings.getSetting("compass.engine.analyzer.stopwords") != null) {
            throw new ConfigurationException(
                    "Old analyzer setting for stopwords, use [compass.engine.analyzer.default.*] instead");
        }
    }
}
