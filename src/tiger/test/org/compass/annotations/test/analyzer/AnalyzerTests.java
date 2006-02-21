package org.compass.annotations.test.analyzer;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class AnalyzerTests extends AbstractAnnotationsTestCase {

    private static final String TEXT = "the quick brown fox jumped over the lazy dogs";

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addPackage("org.compass.annotations.test.analyzer");
    }

    public void testFieldAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = TEXT;
        a.analyzer = "simple";
        session.save(a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        hits = session.find("value:fox");
        assertEquals(1, hits.getLength());

        a = new A();
        a.id = 1;
        a.value = TEXT;
        a.analyzer = null;
        try {
            session.save(a);
            fail();
        } catch (SearchEngineException e) {

        }

        tr.commit();
        session.close();
    }

}
