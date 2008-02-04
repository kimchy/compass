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

package org.compass.core.test.engine.lucene;

import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.AbstractLuceneEngineTests;
import org.compass.core.lucene.engine.transaction.BatchInsertTransaction;
import org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransaction;
import org.compass.core.spi.InternalCompass;

public class BatchInsertTransactionEngineTests extends AbstractLuceneEngineTests {

    protected CompassSettings buildCompassSettings() {
        CompassSettings settings = super.buildCompassSettings();
        settings.setSetting(CompassEnvironment.Transaction.ISOLATION_CLASS, BatchInsertTransaction.class.getName());
        return settings;
    }

    public void testSettings() {
        assertEquals(BatchInsertTransaction.class.getName(), getSettings().getSetting(
                CompassEnvironment.Transaction.ISOLATION_CLASS));
    }

    public void testSimpleBatchInsert() throws Exception {
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        Resource singleId2 = createSingleIdResource2(getSearchEngine());
        getSearchEngine().create(singleId2);
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        getSearchEngine().commit(true);

        getSettings().setSetting(CompassEnvironment.Transaction.ISOLATION_CLASS, ReadCommittedTransaction.class.getName());
        SearchEngineFactory searchEngineFactory = createSearchEngineFactory();
        SearchEngine searchEngine = searchEngineFactory.openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
        searchEngine.begin();
        assertSingleIdResourceExists(searchEngine);
        assertMulitIdResourceExists(searchEngine);
        assertSingleId2ResourceExists(searchEngine);
        assertMulitId2ResourceExists(searchEngine);
        searchEngine.commit(true);
        searchEngine.close();
        searchEngineFactory.close();
    }

    public void testFindFailure() throws Exception {
        getSearchEngine().begin();
        try {
            assertSingleIdResourceExists(getSearchEngine());
            fail();
        } catch (SearchEngineException e) {

        }
        getSearchEngine().commit(true);
    }

    public void testDeleteFailure() throws Exception {
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        try {
            getSearchEngine().delete(singleId);
            fail();
        } catch (SearchEngineException e) {

        }
        getSearchEngine().commit(true);
    }
}
