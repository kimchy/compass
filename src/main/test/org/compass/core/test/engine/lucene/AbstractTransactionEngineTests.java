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
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.lucene.AbstractLuceneEngineTests;

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

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
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

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testMultiIdDoubleEntries() throws Exception {
        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        assertMulitId2ResourceNotExists(getSearchEngine());

        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitId2ResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitId2ResourceExists(getSearchEngine());
        getSearchEngine().delete(multiId2);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceExists(getSearchEngine());
        assertMulitId2ResourceNotExists(getSearchEngine());
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        assertMulitId2ResourceNotExists(getSearchEngine());
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

        getSearchEngine().begin();
        SearchEngineQuery query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        SearchEngineHits hits = query.hits();
        assertEquals(1, hits.getLength());
        Resource r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits();
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID2, r.getProperty(PROPERTY_ID2).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits();
        assertEquals(2, hits.getLength());

        getSearchEngine().delete(r);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits();
        assertEquals(0, hits.getLength());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits();
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

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);
        
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().commit(true);

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

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        getSearchEngine().commit(true);
    }

    public void testDeleteResource2WithCommit() {
        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceNotExists(getSearchEngine());
        assertSingleId2ResourceNotExists(getSearchEngine());
        assertMulitId2ResourceNotExists(getSearchEngine());

        Resource singleId = createSingleIdResource(getSearchEngine());
        getSearchEngine().create(singleId);
        Resource multiId = createMultiIdResource(getSearchEngine());
        getSearchEngine().create(multiId);

        getSearchEngine().commit(true);

        getSearchEngine().begin();
        Resource singleId2 = createSingleIdResource2(getSearchEngine());
        getSearchEngine().create(singleId2);
        Resource multiId2 = createMultiIdResource2(getSearchEngine());
        getSearchEngine().create(multiId2);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleId2ResourceExists(getSearchEngine());
        assertMulitId2ResourceExists(getSearchEngine());

        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleId2ResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId2);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        assertSingleId2ResourceNotExists(getSearchEngine());
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

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().delete(singleId);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertSingleIdResourceNotExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(multiId);
        getSearchEngine().rollback();

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

        getSearchEngine().begin();
        SearchEngineQuery query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        SearchEngineHits hits = query.hits();
        assertEquals(1, hits.getLength());
        Resource r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits();
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());

        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits();
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID2, r.getProperty(PROPERTY_ID2).getStringValue());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        getSearchEngine().delete(r);
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits();
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits();
        assertEquals(0, hits.getLength());
        getSearchEngine().commit(true);

        getSearchEngine().begin();
        assertMulitIdResourceNotExists(getSearchEngine());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL1 + ":" + VALUE_VAL1).toQuery();
        hits = query.hits();
        assertEquals(1, hits.getLength());
        r = hits.getResource(0);
        assertEquals(VALUE_ID1, r.getProperty(PROPERTY_ID1).getStringValue());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL2 + ":" + VALUE_VAL2).toQuery();
        hits = query.hits();
        assertEquals(0, hits.getLength());
        query = getSearchEngine().queryBuilder().queryString(PROPERTY_VAL4 + ":" + VALUE_VAL4).toQuery();
        hits = query.hits();
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

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();


        getSearchEngine().begin();
        getSearchEngine().delete(singleId);
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

        getSearchEngine().prepare();
        getSearchEngine().commit(false);

        getSearchEngine().begin();
        assertSingleIdResourceExists(getSearchEngine());
        assertMulitIdResourceExists(getSearchEngine());
        getSearchEngine().rollback();
    }
}
