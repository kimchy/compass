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

package org.compass.core.engine;

import org.compass.core.Compass;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.converter.DefaultConverterLookup;
import org.compass.core.engine.naming.StaticPropertyNamingStrategy;
import org.compass.core.impl.DefaultCompass;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.metadata.impl.DefaultCompassMetaData;
import org.compass.core.spi.InternalCompass;

/**
 * @author kimchy
 */
public abstract class AbstractSearchEngineTests extends AbstractEngineTests {

    private SearchEngine searchEngine;

    private SearchEngineFactory searchEngineFactory;

    // we have it here for transactional settings
    protected Compass compass;

    public SearchEngine getSearchEngine() {
        return this.searchEngine;
    }

    public SearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.searchEngineFactory = createSearchEngineFactory();
        this.compass = new DefaultCompass(getMapping(), new DefaultConverterLookup(), new DefaultCompassMetaData(), new StaticPropertyNamingStrategy(),
                getSettings(), false, (LuceneSearchEngineFactory) searchEngineFactory);
        this.searchEngine = searchEngineFactory.openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
    }

    protected void tearDown() throws Exception {
        searchEngine.close();
        searchEngineFactory.getIndexManager().clearCache();
        searchEngineFactory.getIndexManager().deleteIndex();
        compass.close();
        super.tearDown();
    }

    protected abstract SearchEngineFactory createSearchEngineFactory();
}
