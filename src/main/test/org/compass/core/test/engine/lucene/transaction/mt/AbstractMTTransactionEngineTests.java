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

package org.compass.core.test.engine.lucene.transaction.mt;

import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.transaction.mt.MTTransactionProcessor;
import org.compass.core.test.engine.lucene.transaction.AbstractTransactionEngineTests;

public abstract class AbstractMTTransactionEngineTests extends AbstractTransactionEngineTests {

    protected CompassSettings buildCompassSettings() {
        CompassSettings settings = super.buildCompassSettings();
        settings.setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.MT.NAME);
        return settings;
    }

    public void testSettings() {
        assertEquals(LuceneEnvironment.Transaction.Processor.MT.NAME, getSettings().getSetting(
                LuceneEnvironment.Transaction.Processor.TYPE));
    }

    public void testSearchEngineTransactionProcessorInstance() {
        getSearchEngine().begin();
        assertTrue(getLuceneSearchEngine().getTransactionProcessor() instanceof MTTransactionProcessor);
        getSearchEngine().rollback();
    }

    public void testDualCreates() throws Exception {
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

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleIdResource2Exists(getSearchEngine());
        assertMulitIdResource2Exists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDualCreatesSameAlias() throws Exception {
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource singleId2 = createSingleIdResource2(getSearchEngine());
        getSearchEngine().create(singleId2);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertSingleIdResource2Exists(getSearchEngine());
        getSearchEngine().commit(true);
    }
}