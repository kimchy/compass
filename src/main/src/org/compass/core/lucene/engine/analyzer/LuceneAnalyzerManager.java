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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSettings;
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

    private HashMap analyzers = new HashMap();

    private Analyzer defaultAnalyzer;

    private Analyzer searchAnalyzer;

    private HashMap aliasAnalyzers = new HashMap();

    private HashMap analyzersFilters = new HashMap();

    private CompassMapping mapping;

    private LuceneSettings luceneSettings;

    public void configure(CompassSettings settings, CompassMapping mapping, LuceneSettings luceneSettings)
            throws SearchEngineException {
        checkNotUsingOldVersionsAnalyzerSettings(settings);
        this.mapping = mapping;
        this.luceneSettings = luceneSettings;
        buildAnalyzersFilters(settings);
        buildAnalyzers(settings, mapping);
    }

    private void buildAnalyzersFilters(CompassSettings settings) {
        Map analyzerFilterSettingGroups = settings.getSettingGroups(LuceneEnvironment.AnalyzerFilter.PREFIX);
        for (Iterator it = analyzerFilterSettingGroups.keySet().iterator(); it.hasNext();) {
            String analyzerFilterName = (String) it.next();
            if (log.isInfoEnabled()) {
                log.info("Building analyzer filter [" + analyzerFilterName + "]");
            }
            CompassSettings analyzerFilterSettings = (CompassSettings) analyzerFilterSettingGroups.get(analyzerFilterName);
            String analyzerFilterType = analyzerFilterSettings.getSetting(LuceneEnvironment.AnalyzerFilter.TYPE);
            if (analyzerFilterType == null) {
                throw new SearchEngineException("Failed to locate analyzer filter [" + analyzerFilterName + "] type, it must be set");
            }
            try {
                if (analyzerFilterType.equals(LuceneEnvironment.AnalyzerFilter.SYNONYM_TYPE)) {
                    analyzerFilterType = SynonymAnalyzerTokenFilterProvider.class.getName();
                }
                LuceneAnalyzerTokenFilterProvider provider =
                        (LuceneAnalyzerTokenFilterProvider) ClassUtils.forName(analyzerFilterType).newInstance();
                provider.configure(analyzerFilterSettings);
                analyzersFilters.put(analyzerFilterName, provider);
            } catch (Exception e) {
                throw new SearchEngineException("Failed to create analyzer filter [" + analyzerFilterName + "]", e);
            }
        }
    }

    private void buildAnalyzers(CompassSettings settings, CompassMapping mapping) {
        Map analyzerSettingGroups = settings.getSettingGroups(LuceneEnvironment.Analyzer.PREFIX);
        for (Iterator it = analyzerSettingGroups.keySet().iterator(); it.hasNext();) {
            String analyzerName = (String) it.next();
            if (log.isInfoEnabled()) {
                log.info("Building analyzer [" + analyzerName + "]");
            }
            Analyzer analyzer = buildAnalyzer(analyzerName, (CompassSettings) analyzerSettingGroups.get(analyzerName));
            analyzers.put(analyzerName, analyzer);
        }
        defaultAnalyzer = (Analyzer) analyzers.get(LuceneEnvironment.Analyzer.DEFAULT_GROUP);
        if (defaultAnalyzer == null) {
            // if no default anayzer is defined, we need to configre one
            defaultAnalyzer = buildAnalyzer(LuceneEnvironment.Analyzer.DEFAULT_GROUP, new CompassSettings());
            analyzers.put(LuceneEnvironment.Analyzer.DEFAULT_GROUP, defaultAnalyzer);
        }
        searchAnalyzer = (Analyzer) analyzers.get(LuceneEnvironment.Analyzer.SEARCH_GROUP);
        if (searchAnalyzer == null) {
            searchAnalyzer = defaultAnalyzer;
        }
        // build the analyzers for the different resources
        buildAnalyzerPerAlias(mapping);
    }

    private void buildAnalyzerPerAlias(CompassMapping mapping)
            throws SearchEngineException {
        ResourceMapping[] resourceMappings = mapping.getRootMappings();
        for (int i = 0; i < resourceMappings.length; i++) {
            ResourceMapping resourceMapping = resourceMappings[i];
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
        return (Analyzer) analyzers.get(analyzerName);
    }

    /**
     * Returns the Lucene {@link Analyzer} for the given alias. Might build a per field analyzer
     * if the resource has more than one analyzer against one of its properties.
     */
    public Analyzer getAnalyzerByAlias(String alias) {
        return (Analyzer) aliasAnalyzers.get(alias);
    }

    public Analyzer getAnalyzerByAliasMustExists(String alias) throws SearchEngineException {
        Analyzer analyzer = (Analyzer) aliasAnalyzers.get(alias);
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
            return (Analyzer) aliasAnalyzers.get(alias);
        }
        ResourceAnalyzerController analyzerController = resourceMapping.getAnalyzerController();
        String analyzerPropertyName = analyzerController.getAnalyzerResourcePropertyName();
        String analyzerName = resource.get(analyzerPropertyName);
        if (analyzerName == null) {
            analyzerName = analyzerController.getNullAnalyzer();
        }
        return buildAnalyzerPerResourcePropertyIfNeeded(resourceMapping, analyzerName);
    }

    public Analyzer getAnalyzerMustExist(String analyzerName) throws SearchEngineException {
        Analyzer analyzer = (Analyzer) analyzers.get(analyzerName);
        if (analyzer == null) {
            throw new SearchEngineException("No analyzer is defined for analyzer name [" + analyzerName + "]");
        }
        return analyzer;
    }

    private Analyzer buildAnalyzer(String analyzerName, CompassSettings settings) {
        String analyzerFactorySetting = settings.getSetting(LuceneEnvironment.Analyzer.FACTORY,
                DefaultLuceneAnalyzerFactory.class.getName());
        LuceneAnalyzerFactory analyzerFactory;
        try {
            analyzerFactory = (LuceneAnalyzerFactory) ClassUtils.forName(analyzerFactorySetting).newInstance();
        } catch (Exception e) {
            throw new SearchEngineException("Cannot create Analyzer factory [" + analyzerFactorySetting
                    + "]. Please verify the analyzer factory setting at [" + LuceneEnvironment.Analyzer.FACTORY + "]",
                    e);
        }
        Analyzer analyzer = analyzerFactory.createAnalyzer(analyzerName, settings);
        String filters = settings.getSetting(LuceneEnvironment.Analyzer.FILTERS);
        if (filters != null) {
            StringTokenizer tokenizer = new StringTokenizer(filters, ",");
            ArrayList filterProviders = new ArrayList();
            while (tokenizer.hasMoreTokens()) {
                String filterProviderLookupName = tokenizer.nextToken();
                if (!StringUtils.hasText(filterProviderLookupName)) {
                    continue;
                }
                LuceneAnalyzerTokenFilterProvider provider =
                        (LuceneAnalyzerTokenFilterProvider) analyzersFilters.get(filterProviderLookupName);
                if (provider == null) {
                    throw new SearchEngineException("Failed to located filter provider [" + filterProviderLookupName
                            + "] for analyzer [" + analyzerName + "]");
                }
                filterProviders.add(provider);
            }
            analyzer = new LuceneAnalyzerFilterWrapper(analyzer,
                    (LuceneAnalyzerTokenFilterProvider[]) filterProviders.toArray(new LuceneAnalyzerTokenFilterProvider[filterProviders.size()]));
        }
        return analyzer;
    }

    private Analyzer buildAnalyzerPerResourcePropertyIfNeeded(ResourceMapping resourceMapping,
                                                              String resourceAnalyzerName) {
        Analyzer resourceAnalyzer = getAnalyzerMustExist(resourceAnalyzerName);
        // create the per field analyzer only if there is one that is
        // specific to a resource property or, the all property is set and
        // is different than the one assigned to the resource.
        if (resourceMapping.hasSpecificAnalyzerPerResourceProperty()
                || (resourceMapping.getAllAnalyzer() != null && !resourceAnalyzerName.equals(resourceMapping.getAllAnalyzer()))) {
            PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(resourceAnalyzer);
            ResourcePropertyMapping[] propertyMappings = resourceMapping.getResourcePropertyMappings();
            for (int j = 0; j < propertyMappings.length; j++) {
                ResourcePropertyMapping propertyMapping = propertyMappings[j];
                if (propertyMapping.getAnalyzer() != null) {
                    Analyzer propertyAnalyzer = getAnalyzer(propertyMapping.getAnalyzer());
                    if (propertyAnalyzer == null) {
                        throw new SearchEngineException("Failed to find analyzer [" + propertyMapping.getAnalyzer()
                                + "] for alias [" + resourceMapping.getAlias() + "] and property ["
                                + propertyMapping.getName() + "]");
                    }
                    perFieldAnalyzerWrapper.addAnalyzer(propertyMapping.getPath(), propertyAnalyzer);
                }
            }
            if (resourceMapping.isAllSupported()) {
                String allP = resourceMapping.getAllProperty();
                if (allP == null) {
                    allP = luceneSettings.getAllProperty();
                }
                Analyzer allAnalyzer = getAnalyzer(resourceMapping.getAllAnalyzer());
                perFieldAnalyzerWrapper.addAnalyzer(allP, allAnalyzer);
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
