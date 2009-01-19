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

package org.compass.core.test.property.storeindex;

import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.lucene.util.LuceneHelper;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class StoreIndexTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/storeindex/mapping.cpm.xml"};
    }

    public void testInternalIdWithStoreNo() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a2", a);

        session.loadResource("a2", id);

        a = (A) session.load("a2", id);
        assertEquals("first test string", a.getValue());

        tr.commit();
        session.close();
    }

    public void testStoreYesIndexTokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a1", a);

        a = (A) session.load("a1", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a1", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTokenized());

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testStoreNoIndexTokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a3", a);

        a = (A) session.load("a3", id);
        assertEquals("first test string", a.getValue());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        Resource r = session.loadResource("a3", id);
        assertNull(r.getValue("mvalue1"));

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testStoreYesIndexUnTokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a4", a);

        a = (A) session.load("a4", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a4", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(false, r.getProperty("mvalue1").isCompressed());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(false, r.getProperty("mvalue1").isTokenized());

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(0, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testStoreComressIndexTokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a5", a);

        a = (A) session.load("a5", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a5", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isCompressed());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTokenized());

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testStoreCompressIndexNo() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a6", a);

        a = (A) session.load("a6", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a6", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(false, r.getProperty("mvalue1").isCompressed());
        assertEquals(false, r.getProperty("mvalue1").isIndexed());
        assertEquals(false, r.getProperty("mvalue1").isTokenized());

        CompassHits hits = session.find("mavlue1:test");
        assertEquals(0, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testTermVectorNo() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a7", a);

        a = (A) session.load("a7", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a7", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(false, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertNull(termInfoVector);

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertNull(termInfoVectors);

        tr.commit();
        session.close();
    }

    public void testTermVectorYes() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a8", a);

        a = (A) session.load("a8", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a8", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertEquals("mvalue1", termInfoVector.getField());
        String[] terms = termInfoVector.getTerms();
        assertEquals(3, terms.length);
        assertEquals("first", terms[0]);
        assertEquals("string", terms[1]);
        assertEquals("test", terms[2]);

        int[] freqs = termInfoVector.getTermFrequencies();
        assertEquals(3, freqs.length);

        try {
            ((TermPositionVector) termInfoVector).getTermPositions(0);
            fail();
        } catch (Exception e) {

        }

        try {
            ((TermPositionVector) termInfoVector).getOffsets(0);
            fail();
        } catch (Exception e) {

        }

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertEquals(1, termInfoVectors.length);

        tr.commit();
        session.close();
    }

    public void testTermVectorYesWithPositions() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a9", a);

        a = (A) session.load("a9", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a9", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertEquals("mvalue1", termInfoVector.getField());
        String[] terms = termInfoVector.getTerms();
        assertEquals(3, terms.length);
        assertEquals("first", terms[0]);
        assertEquals("string", terms[1]);
        assertEquals("test", terms[2]);

        int[] freqs = termInfoVector.getTermFrequencies();
        assertEquals(3, freqs.length);

        int[] positions = ((TermPositionVector) termInfoVector).getTermPositions(0);
        assertNotNull(positions);
        assertEquals(1, positions.length);

        TermVectorOffsetInfo[] offsets = ((TermPositionVector) termInfoVector).getOffsets(0);
        assertNull(offsets);

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertEquals(1, termInfoVectors.length);

        tr.commit();
        session.close();
    }

    public void testTermVectorYesWithOffsets() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a10", a);

        a = (A) session.load("a10", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a10", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertEquals("mvalue1", termInfoVector.getField());
        String[] terms = termInfoVector.getTerms();
        assertEquals(3, terms.length);
        assertEquals("first", terms[0]);
        assertEquals("string", terms[1]);
        assertEquals("test", terms[2]);

        int[] freqs = termInfoVector.getTermFrequencies();
        assertEquals(3, freqs.length);

        int[] positions = ((TermPositionVector) termInfoVector).getTermPositions(0);
        assertNull(positions);

        TermVectorOffsetInfo[] offsets = ((TermPositionVector) termInfoVector).getOffsets(0);
        assertNotNull(offsets);
        assertEquals(1, offsets.length);

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertEquals(1, termInfoVectors.length);

        tr.commit();
        session.close();
    }

    public void testTermVectorYesWithPostionsAndOffsets() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a11", a);

        verifyTermVectorYesWithPostionsAndOffsets(session);

        tr.commit();
        session.close();

        // now test with non transactional data
        session = openSession();
        tr = session.beginTransaction();
        verifyTermVectorYesWithPostionsAndOffsets(session);
        tr.commit();
        session.close();
    }

    private void verifyTermVectorYesWithPostionsAndOffsets(CompassSession session) {
        Long id = new Long(1);
        A a = (A) session.load("a11", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a11", id);
        assertNotNull(r.getValue("mvalue1"));
        assertEquals(true, r.getProperty("mvalue1").isStored());
        assertEquals(true, r.getProperty("mvalue1").isIndexed());
        assertEquals(true, r.getProperty("mvalue1").isTermVectorStored());

        TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, "mvalue1");
        assertEquals("mvalue1", termInfoVector.getField());
        String[] terms = termInfoVector.getTerms();
        assertEquals(3, terms.length);
        assertEquals("first", terms[0]);
        assertEquals("string", terms[1]);
        assertEquals("test", terms[2]);

        int[] freqs = termInfoVector.getTermFrequencies();
        assertEquals(3, freqs.length);

        int[] positions = ((TermPositionVector) termInfoVector).getTermPositions(0);
        assertNotNull(positions);
        assertEquals(1, positions.length);

        TermVectorOffsetInfo[] offsets = ((TermPositionVector) termInfoVector).getOffsets(0);
        assertNotNull(offsets);
        assertEquals(1, offsets.length);

        TermFreqVector[] termInfoVectors = LuceneHelper.getTermFreqVectors(session, r);
        assertEquals(1, termInfoVectors.length);
    }

    public void testStoreNoIndexNotSpecifiedTokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(0, hits.getLength());

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a12", a);

        a = (A) session.load("a12", id);
        assertEquals("first test string", a.getValue());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        Resource r = session.loadResource("a12", id);
        assertNull(r.getValue("mvalue1"));

        hits = session.find("mvalue1:test");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testStoreNoManagedIdFalse() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassHits hits = session.find("mvalue1:test");
        assertEquals(0, hits.getLength());

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("first test string");

        session.save("a13", a);

        a = (A) session.load("a13", id);
        assertNull(a.getValue());

        tr.commit();
        session.close();
    }
}
