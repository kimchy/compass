package org.compass.core.test.analyzer;

import java.io.StringReader;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassToken;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * @author kimchy
 */
public class XsemAnalyzerTests extends AbstractAnalyzerTests {

    public static final String XML_DATA = "<data><id>1</id><value>" + TEXT + "</value><value2>" + TEXT + "</value2></data>";

    public static final String XML_DATA_SIMPLE_ANALYZER = "<data><id>1</id><value>" + TEXT + "</value><value2>" + TEXT + "</value2><analyzer>simple</analyzer></data>";

    protected String[] getMappings() {
        return new String[]{"analyzer/xsem.cpm.xml"};
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias) throws Exception {
        return buildAliasedXmlObject(alias, XML_DATA);
    }

    protected AliasedXmlObject buildAliasedXmlObject(String alias, String data) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(data));
        return new Dom4jAliasedXmlObject(alias, document.getRootElement());
    }


    public void testXmlObjectNoAnalyzer() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("a"));

        CompassHits hits = session.find("a.value:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        // this one will use the simple analyzer
        Resource r = session.loadResource("a", new Long(1));
        CompassToken[] tokens = session.analyzerHelper().setAnalyzer("simple").analyze(r.getValue("value"));
        assertEquals(9, tokens.length);
        assertEquals("the", tokens[0].getTermText());
        assertEquals("quick", tokens[1].getTermText());
        assertEquals("brown", tokens[2].getTermText());
        assertEquals("fox", tokens[3].getTermText());

        // this one will use the default analyzer
        tokens = session.analyzerHelper().setAnalyzerByAlias("a").analyze(r.getValue("value"));
        assertEquals(7, tokens.length);
        assertEquals("quick", tokens[0].getTermText());
        assertEquals("brown", tokens[1].getTermText());
        assertEquals("fox", tokens[2].getTermText());


        tr.commit();
        session.close();
    }

    public void testXmlObjectAnalyzerSetForResource() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("b"));

        CompassHits hits = session.find("b.value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testXmlObjectAnalyzerSetForResourceWithCompassQuery() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("b"));

        CompassHits hits = session.find("b.value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.length());

        // this will only work if we force the analyzer so it won't take into account mappings
        CompassQuery query = session.queryBuilder().queryString("b.value:the").setAnalyzer("default").forceAnalyzer().toQuery();
        assertEquals(0, query.hits().getLength());

        // here we don't force so we will get it
        query = session.queryBuilder().queryString("b.value:the").setAnalyzer("default").toQuery();
        assertEquals(1, query.hits().getLength());

        query = session.queryBuilder().queryString("b.value:the").setAnalyzer("simple").toQuery();
        assertEquals(1, query.hits().length());

        tr.commit();
        session.close();
    }

    public void testXmlObjectAnalyzerSetForProperty() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("d"));

        CompassHits hits = session.find("d.value:the");
        assertEquals(1, hits.getLength());
        hits = session.find("d.value2:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testXmlObjectAnalyzerSetForResourceAndProperty() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("e"));

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("value2:the");
        assertEquals(1, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testXmlObjectAnalyzerController() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("g", XML_DATA_SIMPLE_ANALYZER));

        CompassHits hits = session.find("g.value:the");
        assertEquals(1, hits.getLength());

        try {
            session.save(buildAliasedXmlObject("g", XML_DATA));
            session.flush();
            tr.commit();
            fail();
        } catch (SearchEngineException e) {
            tr.rollback();
        }

        session.close();
    }

    public void testXmlObjectAnalyzerControllerWithNullAnalyzer() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("g", XML_DATA_SIMPLE_ANALYZER));

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        session.save(buildAliasedXmlObject("h"));

        // analyzer controller can't affect query string (since we don't have the resource), just for simple and
        // check that both h and i were saved
        hits = session.queryBuilder().queryString("value:the").setAnalyzer("simple").forceAnalyzer().toQuery().hits();
        assertEquals(2, hits.getLength());

        tr.commit();
        session.close();
    }

    public void testXmlObjectAnalyzerControllerWithNullAnalyzerAndPropertyAnalyzer() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        session.save(buildAliasedXmlObject("i", XML_DATA_SIMPLE_ANALYZER));

        CompassHits hits = session.queryBuilder().queryString("value:the").setAnalyzer("simple").forceAnalyzer().toQuery().hits();
        assertEquals(0, hits.getLength());

        tr.commit();
        session.close();
    }

}
