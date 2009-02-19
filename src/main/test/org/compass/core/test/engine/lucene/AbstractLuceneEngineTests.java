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

package org.compass.core.test.engine.lucene;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.naming.DefaultPropertyNamingStrategyFactory;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.PropertyNamingStrategyFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.test.engine.AbstractSearchEngineTests;

/**
 * @author kimchy
 */
public abstract class AbstractLuceneEngineTests extends AbstractSearchEngineTests {

    protected SearchEngineFactory createSearchEngineFactory() {
        PropertyNamingStrategyFactory propertyNamingStrategyFactory = new DefaultPropertyNamingStrategyFactory();
        PropertyNamingStrategy propertyNamingStrategy = propertyNamingStrategyFactory
                .createNamingStrategy(getSettings());

        SearchEngineFactory searchEngineFactory = new LuceneSearchEngineFactory(propertyNamingStrategy,
                getSettings(), getMapping(), createExecutorManager());
        return searchEngineFactory;
    }

    protected CompassSettings buildCompassSettings() {
        CompassSettings settings = super.buildCompassSettings();
        settings.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        settings.setSetting(LuceneEnvironment.DEFAULT_SEARCH, PROPERTY_VAL1);
        settings.setBooleanSetting(LuceneEnvironment.Optimizer.SCHEDULE, false);
        return settings;
    }

}
