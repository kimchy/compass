package org.compass.core.test.termfreqvector.simple1;

import org.apache.lucene.index.TermFreqVector;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.lucene.util.LuceneHelper;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class TermFreqVectorMultiSubIndexTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"termfreqvector/simple1/mapping.cpm.xml"};
    }

    public void testTermFreqVectorMultiSubIndex() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        for (int i = 1; i < 5; i++) {
            Long id = new Long(i);
            A a = new A();
            a.setId(id);
            a.setValue1("test1");
            a.setValue2("test2");
            session.save("a5", a);
        }

        for (int i = 1; i < 5; i++) {
            Long id = new Long(i);
            A a = new A();
            a.setId(id);
            a.setValue1("test1");
            a.setValue2("test2");
            session.save("a6", a);
        }

        CompassHits hits = session.find("alias:a5");

        for (int i = 0; i < hits.getLength(); i++) {
            Resource r = hits.hit(i).getResource();
            TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, CompassEnvironment.All.DEFAULT_NAME);
            assertNotNull(termInfoVector);
        }

        hits = session.find("alias:a6");

        //ak13: the bad case is here:
        for (int i = 0; i < hits.getLength(); i++) {
            Resource r = hits.hit(i).getResource();
            TermFreqVector termInfoVector = LuceneHelper.getTermFreqVector(session, r, CompassEnvironment.All.DEFAULT_NAME);
            assertNotNull(termInfoVector);
        }


        tr.commit();
        session.close();
    }
}
