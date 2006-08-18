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

package org.compass.core.test.property;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
public class PropertyTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "property/NoInternalId.cpm.xml", "property/SimpleTypes.cpm.xml",
                "property/MultipleMetaData.cpm.xml", "property/ReaderType.cpm.xml", "property/A.cpm.xml",
                "property/BinaryType.cpm.xml", "property/PropertyOverride.cpm.xml" };
    }

    public void testNoInternalId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        NoInternalId o = new NoInternalId();
        o.setId(id);
        o.setValue("test");
        session.save(o);

        o = (NoInternalId) session.load(NoInternalId.class, id);
        assertEquals("test", o.getValue());

        session.delete(o);

        tr.commit();
        session.close();
    }

    public void testSimpleTypes() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        SimpleTypes o = new SimpleTypes();
        o.setId(id);
        o.setOBigDecimal(new BigDecimal(22.22d));
        o.setOBoolean(Boolean.TRUE);
        o.setOByte(new Byte((byte) 1));
        o.setOChar(new Character('A'));
        Date date = new Date();
        o.setODate(date);
        Calendar c = Calendar.getInstance();
        c.set(2001, 1, 1);
        o.setOCalendar(c);
        o.setODouble(new Double(2.456d));
        o.setOFile(new File("test/testfile"));
        o.setOInteger(new Integer(23456));
        o.setOLong(new Long(345345));
        o.setOShort(new Short((short) 234));
        o.setOString("TEST");
        o.setOStringBuffer(new StringBuffer("TEST STRING BUFFER"));
        o.setOURL(new URL("http://www.opensymphony.com/compass"));
        o.setSBoolean(true);
        o.setSByte((byte) 2);
        o.setSChar('B');
        o.setSDouble(12.34456d);
        o.setSInt(8786095);
        o.setSLong(234234);
        o.setSShort((short) 34554);
        o.setSFloat(23.45f);
        o.setOFloat(new Float(567.567f));
        o.setOLocale(Locale.ENGLISH);

        session.save(o);

        o = (SimpleTypes) session.load(SimpleTypes.class, id);
        assertEquals(new BigDecimal(22.22d), o.getOBigDecimal());
        assertEquals(Boolean.TRUE, o.getOBoolean());
        assertEquals(new Byte((byte) 1), o.getOByte());
        assertEquals(new Character('A'), o.getOChar());
        assertEquals(date, o.getODate());
        assertEquals(c, o.getOCalendar());
        assertEquals(new Double(2.456d), o.getODouble());
        assertEquals(new File("test/testfile").getAbsolutePath(), o.getOFile().getAbsolutePath());
        assertEquals(new Integer(23456), o.getOInteger());
        assertEquals(new Long(345345), o.getOLong());
        assertEquals(new Short((short) 234), o.getOShort());
        assertEquals("TEST", o.getOString());
        assertEquals("TEST STRING BUFFER", o.getOStringBuffer().toString());
        assertEquals(new URL("http://www.opensymphony.com/compass").toExternalForm(), o.getOURL().toExternalForm());
        assertEquals(true, o.isSBoolean());
        assertEquals((byte) 2, o.getSByte());
        assertEquals('B', o.getSChar());
        assertEquals(12.34456d, o.getSDouble(), 0.000001d);
        assertEquals(8786095, o.getSInt());
        assertEquals(234234, o.getSLong());
        assertEquals((short) 34554, o.getSShort());
        assertEquals(23.45f, o.getSFloat(), 0.000001f);
        assertEquals(new Float(567.567f), o.getOFloat());
        assertEquals(Locale.ENGLISH, o.getOLocale());

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        o = (SimpleTypes) session.load(SimpleTypes.class, id);
        assertEquals(new BigDecimal(22.22d), o.getOBigDecimal());
        assertEquals(Boolean.TRUE, o.getOBoolean());
        assertEquals(new Byte((byte) 1), o.getOByte());
        assertEquals(new Character('A'), o.getOChar());
        assertEquals(date, o.getODate());
        assertEquals(c, o.getOCalendar());
        assertEquals(new Double(2.456d), o.getODouble());
        assertEquals(new File("test/testfile").getAbsolutePath(), o.getOFile().getAbsolutePath());
        assertEquals(new Integer(23456), o.getOInteger());
        assertEquals(new Long(345345), o.getOLong());
        assertEquals(new Short((short) 234), o.getOShort());
        assertEquals("TEST", o.getOString());
        assertEquals("TEST STRING BUFFER", o.getOStringBuffer().toString());
        assertEquals(new URL("http://www.opensymphony.com/compass").toExternalForm(), o.getOURL().toExternalForm());
        assertEquals(true, o.isSBoolean());
        assertEquals((byte) 2, o.getSByte());
        assertEquals('B', o.getSChar());
        assertEquals(12.34456d, o.getSDouble(), 0.000001d);
        assertEquals(8786095, o.getSInt());
        assertEquals(234234, o.getSLong());
        assertEquals((short) 34554, o.getSShort());
        assertEquals(23.45f, o.getSFloat(), 0.000001f);
        assertEquals(new Float(567.567f), o.getOFloat());
        assertEquals(Locale.ENGLISH, o.getOLocale());

        tr.commit();
        session.close();
    }

    public void testMultipleMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id1 = new Long(1);
        MultipleMetaData o1 = new MultipleMetaData();
        o1.setId(id1);
        o1.setValue1("test1");
        o1.setValue2("test2");
        session.save(o1);
        Long id2 = new Long(2);
        MultipleMetaData o2 = new MultipleMetaData();
        o2.setId(id2);
        o2.setValue1("test1");
        o2.setValue2("testNO");
        session.save(o2);

        CompassHits list = session.find("join2:test1");
        assertEquals(2, list.getLength());

        list = session.find("value21:testNO");
        assertEquals(1, list.getLength());

        tr.commit();
    }

    public void testReader() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ReaderType o = new ReaderType();
        o.setId(id);
        session.save(o);

        o = (ReaderType) session.load(ReaderType.class, id);
        assertEquals(id, o.getId());

        tr.commit();
    }

    public void testInputStream() throws IOException {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        InputStreamType o = new InputStreamType();
        o.setId(id);

        byte[] bytes = new byte[] { 1, 2, 3 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        o.setInputStream(inputStream);
        session.save(o);

        tr.commit();

        tr = session.beginTransaction();

        o = (InputStreamType) session.load(InputStreamType.class, id);
        assertEquals(id, o.getId());
        assertNotNull(o.getInputStream());
        assertEquals(1, o.getInputStream().read());
        assertEquals(2, o.getInputStream().read());
        assertEquals(3, o.getInputStream().read());

        tr.commit();
    }

    public void testBinaryArray() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        byte[] bytes = new byte[] { 1, 2, 3 };
        Byte[] oBytes = new Byte[] { new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3) };
        BinaryArrayType o = new BinaryArrayType();
        o.setId(id);
        o.setBytes(bytes);
        o.setOBytes(oBytes);

        session.save(o);

        o = (BinaryArrayType) session.load(BinaryArrayType.class, id);
        assertNotNull(o.getBytes());
        assertEquals(3, o.getBytes().length);
        assertEquals(1, o.getBytes()[0]);
        assertEquals(2, o.getBytes()[1]);
        assertEquals(3, o.getBytes()[2]);

        assertEquals(3, o.getOBytes().length);
        assertEquals(new Byte((byte) 1), o.getOBytes()[0]);
        assertEquals(new Byte((byte) 2), o.getOBytes()[1]);
        assertEquals(new Byte((byte) 3), o.getOBytes()[2]);

        tr.commit();
        session.close();
    }

    public void testOverrideIdsAndProperties() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        PropertyOverride po = new PropertyOverride();
        po.setId(id);
        po.setValue("value");

        session.save("po", po);

        Resource r = session.loadResource("po", id);
        assertNull(r.getProperty("wrongId"));
        assertNull(r.getProperty("wrongValue"));
        assertNotNull(r.getProperty("overrideId"));
        assertNotNull(r.getProperty("overrideValue"));

        id = new Long(1);
        po = new PropertyOverride();
        po.setId(id);
        po.setValue("value");

        session.save("po1", po);

        r = session.loadResource("po1", id);
        assertNull(r.getProperty("wrongId"));
        assertNotNull(r.getProperty("wrongValue"));
        assertNotNull(r.getProperty("overrideId"));
        assertNotNull(r.getProperty("overrideValue"));

        tr.commit();
        session.close();
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
        assertNotNull(r.get("mvalue1"));
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
        assertNull(r.get("mvalue1"));

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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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
        assertNotNull(r.get("mvalue1"));
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

        testTermVectorYesWithPostionsAndOffsets(session);

        tr.commit();
        session.close();

        // now test with non transactional data
        session = openSession();
        tr = session.beginTransaction();
        testTermVectorYesWithPostionsAndOffsets(session);
        tr.commit();
        session.close();
    }

    private void testTermVectorYesWithPostionsAndOffsets(CompassSession session) {
        Long id = new Long(1);
        A a = (A) session.load("a11", id);
        assertEquals("first test string", a.getValue());
        Resource r = session.loadResource("a11", id);
        assertNotNull(r.get("mvalue1"));
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
        assertNull(r.get("mvalue1"));

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
