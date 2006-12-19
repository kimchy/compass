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

import java.util.ArrayList;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.spi.InternalCompass;

/**
 * @author kimchy
 */
public abstract class AbstractReadCommittedTransactionTests extends AbstractTransactionEngineTests {

    // don't test it for now, looooooooooong
    public void XtestFindConsistent() throws Exception {
        // create 475 resources, with incremental ids. Commit the data every one
        // and a while
        int index = 0;
        int commitFactor = 2;
        for (; index < 475; index++) {
            Resource singleId = getSearchEngine().createResource(ALIAS_SINGLE);
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_ID1, "" + index, Property.Store.YES,
                    Property.Index.UN_TOKENIZED));
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                    Property.Index.TOKENIZED));
            getSearchEngine().save(singleId);
            if (index % commitFactor == 0) {
                getSearchEngine().commit(true);
                getSearchEngine().begin();
                if (++commitFactor > 20) {
                    commitFactor = 2;
                }
            }
        }
        // the 476 resource has special data in it, that we will check later on
        Resource singleId = getSearchEngine().createResource(ALIAS_SINGLE);
        singleId.addProperty(getSearchEngine().createProperty(PROPERTY_ID1, "" + index++, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        singleId.addProperty(getSearchEngine().createProperty(PROPERTY_VAL1, "he's my special boy", Property.Store.YES,
                Property.Index.TOKENIZED));
        getSearchEngine().save(singleId);
        // wrap it up to 1000 resources, committing again every once in a while
        for (; index < 1000; index++) {
            singleId = getSearchEngine().createResource(ALIAS_SINGLE);
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_ID1, "" + index, Property.Store.YES,
                    Property.Index.UN_TOKENIZED));
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                    Property.Index.TOKENIZED));
            getSearchEngine().save(singleId);
            if (index % commitFactor == 0) {
                getSearchEngine().commit(true);
                getSearchEngine().begin();
                if (++commitFactor > 20) {
                    commitFactor = 2;
                }
            }
        }
        getSearchEngine().commit(true);

        // start a new index engine, and verify that the special resource exists
        // there, keep the engine transaction open, and keep the hits
        SearchEngine searchEngine = getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
        searchEngine.begin();
        SearchEngineQuery query = searchEngine.queryBuilder().queryString("special").toQuery();
        SearchEngineHits hits = query.hits();
        assertEquals(1, hits.getLength());

        // while the previous index engine is open, add another 1000 resources,
        // again committing every 10 resources
        getSearchEngine().begin();
        for (index = 1000; index < 2000; index++) {
            singleId = getSearchEngine().createResource(ALIAS_SINGLE);
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_ID1, "" + index, Property.Store.YES,
                    Property.Index.UN_TOKENIZED));
            singleId.addProperty(getSearchEngine().createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                    Property.Index.TOKENIZED));
            getSearchEngine().save(singleId);
            if (index % 10 == 0) {
                getSearchEngine().commit(true);
                getSearchEngine().begin();
            }
        }
        getSearchEngine().commit(true);

        // verify that the hits are still valid, and read the resource for the
        // first time (so no caching is involved) and verify that the data is
        // the same as the special resource. It checks that the resources didn't
        // get scrambled
        assertEquals(1, hits.getLength());
        assertEquals("475", hits.getResource(0).getProperty(PROPERTY_ID1).getStringValue());
        assertEquals("he's my special boy", hits.getResource(0).getProperty(PROPERTY_VAL1).getStringValue());
        searchEngine.commit(true);
    }

    public void testTermDocs() throws Exception {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        // create an index with data and commit it
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        // start one index engine again, and perform reads
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        LuceneSearchEngineInternalSearch internalSearch = (LuceneSearchEngineInternalSearch) getSearchEngine().internalSearch(null, null);
        TermEnum termEnum = internalSearch.getReader().terms(new Term(PROPERTY_VAL1, ""));
        try {
            ArrayList tempList = new ArrayList();
            while (PROPERTY_VAL1.equals(termEnum.term().field())) {
                tempList.add(termEnum.term().text());

                if (!termEnum.next()) {
                    break;
                }
            }
            assertEquals(1, tempList.size());
            assertEquals("val1value", tempList.get(0));
        } finally {
            termEnum.close();
        }

        getSearchEngine().commit(true);
    }

    public void testConcurrentReads() throws Exception {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        // create an index with data and commit it
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        // start one index engine again, and perform reads
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        // start another index engine, and perform reads (checks that is is not
        // locked - conccurrent)
        SearchEngine searchEngine = getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
        searchEngine.begin();
        assertSingleIdResourceExists(searchEngine);
        assertMulitIdResourceExists(searchEngine);

        searchEngine.commit(true);
        getSearchEngine().rollback();
    }

    public void testNoDirtyRead() throws Exception {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        // create an index with data, don't commit it yet
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        // start another index engine and check that we can read (as well not
        // read the other transaction data)
        SearchEngine searchEngine = getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
        searchEngine.begin();

        assertSingleIdResourceNotExists(searchEngine);
        assertMulitIdResourceNotExists(searchEngine);
        searchEngine.commit(true);

        getSearchEngine().commit(true);
    }

    public void testUnrepeatableReads() throws Exception {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        // start an index engine, with an index that has no data
        SearchEngine searchEngine = getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(((InternalCompass) compass).getSettings()));
        searchEngine.begin();
        assertSingleIdResourceNotExists(searchEngine);
        assertMulitIdResourceNotExists(searchEngine);

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        // check for repeatable reads
        assertSingleIdResourceExists(searchEngine);
        assertMulitIdResourceExists(searchEngine);
        searchEngine.commit(true);
    }

    public void testTwoPhaseCreatePrepareRollback() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        getSearchEngine().prepare();
        getSearchEngine().rollback();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testTwoPhaseDeletePrepareRollback() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();

        
        getSearchEngine().begin();
        getSearchEngine().delete(singleId);
        assertSingleIdResourceNotExists(getSearchEngine());
        getSearchEngine().prepare();
        getSearchEngine().rollback();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testTwoPhasePrepareCommit() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }
}
