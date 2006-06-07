package org.compass.core.test.analyzer;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassToken;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class ResourceAnalyzerTests extends AbstractAnalyzerTests {

    protected String[] getMappings() {
        return new String[]{"analyzer/resource.cpm.xml"};
    }

    public void testResourceNoAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("a");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        // this one will use the simple analyzer
        CompassToken[] tokens = session.analyzerHelper().setAnalyzer("simple").analyze(r.get("value"));
        assertEquals(9, tokens.length);
        assertEquals("the", tokens[0].getTermText());
        assertEquals("quick", tokens[1].getTermText());
        assertEquals("brown", tokens[2].getTermText());
        assertEquals("fox", tokens[3].getTermText());

        // this one will use the default analyzer
        tokens = session.analyzerHelper().setAnalyzerByAlias("a").analyze(r.get("value"));
        assertEquals(7, tokens.length);
        assertEquals("quick", tokens[0].getTermText());
        assertEquals("brown", tokens[1].getTermText());
        assertEquals("fox", tokens[2].getTermText());

        tr.commit();

    }

    public void testResourceAnalyzerSetForResource() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("b");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerSetForResourceWithCompassQuery() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("b");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.length());

        CompassQuery query = session.queryBuilder().queryString("value:the").setAnalyzer("default").toQuery();
        assertEquals(0, query.hits().getLength());

        query = session.queryBuilder().queryString("value:the").setAnalyzer("simple").toQuery();
        assertEquals(1, query.hits().length());

        tr.commit();
    }

    public void testResourceAnalyzerSetForProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("d");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("value2", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());
        hits = session.find("value2:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerSetForResourceAndProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("e");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("value2", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("value2:the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerOnlyAllAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("f");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerController() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        r = session.createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        try {
            session.save(r);
            fail();
        } catch (SearchEngineException e) {

        }

        tr.commit();
    }

    public void testResourceAnalyzerControllerWithNullAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        r = session.createResource("h");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);
        hits = session.find("value:the");
        assertEquals(2, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerControllerWithNullAnalyzerAndPropertyAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Resource r = session.createResource("i");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());

        tr.commit();
    }
}
