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

package org.compass.core.test.engine.lucene.transaction;

import org.compass.core.Resource;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.test.engine.lucene.AbstractLuceneEngineTests;

/**
 * @author kimchy
 */
public abstract class AbstractTransactionEngineTests extends AbstractLuceneEngineTests {

    public void testCreateResource() {
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testSaveResource() {
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertSingleIdResourceOriginal(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        getSearchEngine().rollback();

        sleepForChangesToOccur();

        getSearchEngine().begin();
        singleId = createUpdatedSingleIdResource(getSearchEngine());
        getSearchEngine().save(singleId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertSingleIdResourceUpdated(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testDeleteResource() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testMultiIdDoubleEntries() throws Exception {
        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        assertMulitIdResource2NotExists(getSearchEngine());

        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResource2NotExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResource2Exists(getSearchEngine());
        getSearchEngine().delete(multiId2);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResource2NotExists(getSearchEngine());
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        assertMulitIdResource2NotExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testFindResource() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        SearchEngineQuery query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        SearchEngineHits hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        Resource r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID2, r.getProperty(PROPERTY_ID2).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(2, hits.getLength());

        getSearchEngine().delete(r);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(0, hits.getLength());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        getSearchEngine().rollback();
    }

    public void testCreateResourceWithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testCreateResourceWithRollback() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().rollback();

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testDeleteResourceWithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDeleteQueryWithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(createSinlgeResourceDeleteQuery(getSearchEngine()));
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(createMultiResourceDeteteQuery(getSearchEngine()));
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDoubleDeletes() throws Exception {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().delete(multiId);
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDeleteResource2WithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        assertSingleIdResource2NotExists(getSearchEngine());
        assertMulitIdResource2NotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        Resource singleId2 = createSingleIdResource2(getSearchEngine());
        getSearchEngine().create(singleId2);
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleIdResource2Exists(getSearchEngine());
        assertMulitIdResource2Exists(getSearchEngine());

        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleIdResource2Exists(getSearchEngine());
        getSearchEngine().delete(singleId2);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleIdResource2NotExists(getSearchEngine());
        getSearchEngine().commit(true);

    }

    public void testDeleteResourceWithRollback() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().rollback();

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDeleteQueryWithRollback() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(createSinlgeResourceDeleteQuery(getSearchEngine()));
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        getSearchEngine().delete(createMultiResourceDeteteQuery(getSearchEngine()));
        getSearchEngine().rollback();

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testFindResourceWithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        SearchEngineQuery query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        SearchEngineHits hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        Resource r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID2, r.getProperty(PROPERTY_ID2).getStringValue());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        getSearchEngine().delete(r);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(0, hits.getLength());
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(0, hits.getLength());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits(getSearchEngine());
        assertEquals(1, hits.getLength());
        getSearchEngine().commit(true);
    }

    public void testTwoPhaseCreatePrepareRollback() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        getSearchEngine().prepare();
        getSearchEngine().rollback();

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testTwoPhaseSavePrepareRollback() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceOriginal(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        singleId = createUpdatedSingleIdResource(getSearchEngine());
        getSearchEngine().save(singleId);
        getSearchEngine().prepare();
        getSearchEngine().rollback();

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceOriginal(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testTwoPhaseDeletePrepareRollback() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();


        sleepForChangesToOccur();

        getSearchEngine().begin();
        getSearchEngine().delete(singleId);
        getSearchEngine().prepare();
        getSearchEngine().rollback();

        sleepForChangesToOccur();

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

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testTwoPhaseSavePrepareCommit() {
        // create an index with data and commit it
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceOriginal(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        singleId = createUpdatedSingleIdResource(getSearchEngine());
        getSearchEngine().save(singleId);
        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        sleepForChangesToOccur();

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertSingleIdResourceUpdated(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitIdResourceOriginal(getSearchEngine());
        getSearchEngine().rollback();
    }

    public void testFlushCommit() {
        // create a search search engine
        SearchEngine searchSearchEngine = createNewSearchEngine();
        searchSearchEngine.begin();

        // verify that there is no resources there
        assertSingleIdResourceNotExists(searchSearchEngine);
        assertMulitIdResourceNotExists(searchSearchEngine);

        // start the search engine the perfoms the actual dirty operations
        getSearchEngine().begin();
        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        sleepForChangesToOccur();

        // clear the cache and verify that we don't see the changes in the search SearchEngine
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResourceNotExists(searchSearchEngine);
        assertMulitIdResourceNotExists(searchSearchEngine);

        // flush commit, and now check that we see the changes in the search SearchEgnine
        getSearchEngine().flushCommit();
        sleepForChangesToOccur();
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResourceExists(searchSearchEngine);
        assertMulitIdResourceExists(searchSearchEngine);

        // create new resources (after flush commit), make sure we can do that
        Resource singleId2 = createSingleIdResource2(getSearchEngine());
        getSearchEngine().create(singleId2);
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        sleepForChangesToOccur();

        // verify that we don't see them in the search SearchEngine
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResource2NotExists(searchSearchEngine);
        assertMulitIdResource2NotExists(searchSearchEngine);

        // flush commit and see that we can see them
        getSearchEngine().flushCommit();
        sleepForChangesToOccur();
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResource2Exists(searchSearchEngine);
        assertMulitIdResource2Exists(searchSearchEngine);

        // now delete the resource
        getSearchEngine().delete(singleId);
        getSearchEngine().delete(multiId);
        sleepForChangesToOccur();

        // check that we don't see it in the search SearchEngine
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResourceExists(searchSearchEngine);
        assertMulitIdResourceExists(searchSearchEngine);

        // now flush commit, and see that we don't see it in the search SearchEngine
        getSearchEngine().flushCommit(ALIAS_SINGLE);
        sleepForChangesToOccur();
        getSearchEngineFactory().getIndexManager().clearCache();
        assertSingleIdResourceNotExists(searchSearchEngine);
        assertMulitIdResourceExists(searchSearchEngine);
        assertSingleIdResource2Exists(searchSearchEngine);
        assertMulitIdResource2Exists(searchSearchEngine);

        searchSearchEngine.commit(true);
        getSearchEngine().commit(true);
    }

    protected void sleepForChangesToOccur() {
    }
}
