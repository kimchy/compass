package org.compass.core.test.resource.termfreqvector;

import org.apache.lucene.index.TermFreqVector;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.lucene.util.LuceneHelper;
import org.compass.core.test.AbstractTestCase;

public class TermFreqVectorTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"resource/termfreqvector/rsem.cpm.xml"};
    }

    public void testAllWithTermVectorRSEM() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        for (int i = 0; i < 5; i++) {
            Resource resA = getResourceFactory().createResource("resource_a");
            resA.addProperty("id", "A" + i);
            resA.addProperty("code", "Acode");
            resA.addProperty("prop_all", "Aall");

            session.save(resA);

            Resource resB = getResourceFactory().createResource("resource_b");
            resB.addProperty("id", "B" + i);
            resB.addProperty("code", "Bcode");
            resB.addProperty("prop_all", "Ball");

            session.save(resB);
        }

        try {
            Resource rA = session.loadResource("resource_a", "A0");
            TermFreqVector termInfoVectorA = LuceneHelper.getTermFreqVector(session, rA, "code");
            assertNotNull(termInfoVectorA);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            Resource rB = session.loadResource("resource_b", "B0");
            TermFreqVector termInfoVectorB = LuceneHelper.getTermFreqVector(session, rB, "code");
            assertNotNull(termInfoVectorB);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            Resource rB = session.loadResource("resource_b", "B3");
            TermFreqVector termInfoVectorB = LuceneHelper.getTermFreqVector(session, rB, "code");
            assertNotNull(termInfoVectorB);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        tr.commit();
        session.close();
    }

    public void testAllWithTermVectorRSEMFind() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        for (int i = 0; i < 5; i++) {
            Resource resA = getResourceFactory().createResource("resource_a");
            resA.addProperty("id", "A" + i);
            resA.addProperty("code", "Acode");
            resA.addProperty("prop_all", "Aall");

            session.save(resA);

            Resource resB = getResourceFactory().createResource("resource_b");
            resB.addProperty("id", "B" + i);
            resB.addProperty("code", "Bcode");
            resB.addProperty("prop_all", "Ball");

            session.save(resB);
        }


        CompassHits hits = session.find("alias:resource_a");

        for (int i = 0; i < hits.getLength(); i++) {
            Resource rA = hits.hit(i).getResource();
            TermFreqVector termInfoVectorA = LuceneHelper.getTermFreqVector(session, rA, "code");
            assertNotNull(termInfoVectorA);
        }

        hits = session.find("alias:resource_b");
        for (int i = 0; i < hits.getLength(); i++) {
            Resource rB = hits.hit(i).getResource();
            TermFreqVector termInfoVectorB = LuceneHelper.getTermFreqVector(session, rB, "code");
            assertNotNull(termInfoVectorB);
        }

        tr.commit();
        session.close();
    }
}
