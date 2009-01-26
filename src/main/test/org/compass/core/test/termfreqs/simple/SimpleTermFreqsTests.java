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

package org.compass.core.test.termfreqs.simple;

import org.compass.core.CompassSession;
import org.compass.core.CompassTermFreq;
import org.compass.core.CompassTermFreqsBuilder;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleTermFreqsTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"termfreqs/simple/mapping.cpm.xml"};
    }

    public void testNoFreqs() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value2").toTermFreqs();
        assertEquals(0, termFreqs.length);

        session.save(new A(1, "test"));
        termFreqs = session.termFreqsBuilder("value2").toTermFreqs();
        assertEquals(0, termFreqs.length);

        tr.commit();
        session.close();
    }

    public void testSimpleFreqs() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(new A(1, "bbb"));
        session.save(new A(2, "aaa"));
        session.save(new A(3, "bbb"));

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value").toTermFreqs();
        assertEquals(2, termFreqs.length);
        assertEquals("bbb", termFreqs[0].getTerm());
        assertEquals("value", termFreqs[0].getPropertyName());
        assertEquals(2, (int) termFreqs[0].getFreq());
        assertEquals("aaa", termFreqs[1].getTerm());
        assertEquals(1, (int) termFreqs[1].getFreq());
        assertEquals("value", termFreqs[1].getPropertyName());

        tr.commit();
        session.close();

        // delete the second A
        session = openSession();
        tr = session.beginTransaction();

        session.delete(A.class, 2);
        termFreqs = session.termFreqsBuilder("value").toTermFreqs();
        assertEquals(2, termFreqs.length);

        tr.commit();
        session.close();

        // only after optimization the term freqs are updated in Lucene
        getCompass().getSearchEngineOptimizer().optimize(1);

        // verify that the deletion affected the termFreqs
        session = openSession();
        tr = session.beginTransaction();

        A a = session.get(A.class, 2);
        assertNull(a);
        termFreqs = session.termFreqsBuilder("value").toTermFreqs();
        assertEquals(1, termFreqs.length);

        tr.commit();
        session.close();
    }

    public void testSimpleFreqsSortTerm() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(new A(1, "bbb"));
        session.save(new A(2, "aaa"));
        session.save(new A(3, "bbb"));

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value").setSort(CompassTermFreqsBuilder.Sort.TERM).toTermFreqs();
        assertEquals(2, termFreqs.length);
        assertEquals("aaa", termFreqs[0].getTerm());
        assertEquals(1, (int) termFreqs[0].getFreq());
        assertEquals("bbb", termFreqs[1].getTerm());
        assertEquals(2, (int) termFreqs[1].getFreq());

        tr.commit();
        session.close();
    }

    public void testSimpleFreqsWithSize() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(new A(1, "test"));
        session.save(new A(2, "test1"));
        session.save(new A(3, "test"));

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value").setSize(1).toTermFreqs();
        assertEquals(1, termFreqs.length);
        assertEquals("test", termFreqs[0].getTerm());
        assertEquals(2, (int) termFreqs[0].getFreq());

        tr.commit();
        session.close();
    }

    public void testSimpleFreqsWithNormalize() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(new A(1, "test"));
        session.save(new A(2, "test1"));
        session.save(new A(3, "test"));
        session.save(new A(4, "test"));
        session.save(new A(5, "test"));
        session.save(new A(6, "test"));
        session.save(new A(7, "test2"));
        session.save(new A(8, "test2"));

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value").normalize(0, 1).toTermFreqs();
        assertEquals(3, termFreqs.length);
        assertEquals("test", termFreqs[0].getTerm());
        assertEquals(1.0, termFreqs[0].getFreq(), 0.001);
        assertEquals("test2", termFreqs[1].getTerm());
        assertEquals(0.25, termFreqs[1].getFreq(), 0.001);
        assertEquals("test1", termFreqs[2].getTerm());
        assertEquals(0.0, termFreqs[2].getFreq(), 0.001);

        termFreqs = session.termFreqsBuilder("value").normalize(1, 10).toTermFreqs();
        assertEquals(3, termFreqs.length);
        assertEquals("test", termFreqs[0].getTerm());
        assertEquals(10.0, termFreqs[0].getFreq(), 0.001);
        assertEquals("test2", termFreqs[1].getTerm());
        assertEquals(3.25, termFreqs[1].getFreq(), 0.001);
        assertEquals("test1", termFreqs[2].getTerm());
        assertEquals(1.0, (int) termFreqs[2].getFreq(), 0.001);

        tr.commit();
        session.close();
    }

    public void testSimpleFreqsWithMultiplePropertyNames() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(new A(1, "test", "name1"));
        session.save(new A(2, "test1", "name2"));
        session.save(new A(3, "test", "name2"));

        CompassTermFreq[] termFreqs = session.termFreqsBuilder("value", "name").toTermFreqs();
        assertEquals(4, termFreqs.length);
        assertEquals("test", termFreqs[0].getTerm());
        assertEquals(2, (int) termFreqs[0].getFreq());
        assertEquals("value", termFreqs[0].getPropertyName());
        assertEquals("name2", termFreqs[1].getTerm());
        assertEquals(2, (int) termFreqs[1].getFreq());
        assertEquals("name", termFreqs[1].getPropertyName());
        assertEquals("name1", termFreqs[2].getTerm());
        assertEquals(1, (int) termFreqs[2].getFreq());
        assertEquals("name", termFreqs[2].getPropertyName());
        assertEquals("test1", termFreqs[3].getTerm());
        assertEquals(1, (int) termFreqs[3].getFreq());
        assertEquals("value", termFreqs[3].getPropertyName());

        tr.commit();
        session.close();
    }
}
