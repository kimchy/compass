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

package org.compass.core.test.analyzer;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassToken;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 */
public class AnalyzerTests extends AbstractTestCase {

    private static final String TEXT = "the quick brown fox jumped over the lazy dogs";

    protected String[] getMappings() {
        return new String[] { "analyzer/resource.cpm.xml", "analyzer/osem.cpm.xml" };
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, "simple",
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, LuceneEnvironment.Analyzer.SEARCH_GROUP,
                new String[] { LuceneEnvironment.Analyzer.TYPE },
                new String[] { LuceneEnvironment.Analyzer.CoreTypes.SIMPLE });
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

    public void testClassNoAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        session.save("a1", a);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForResource() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        session.save("a2", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        session.save("a3", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());
        hits = session.find("value2:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForResourceAndProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        session.save("a4", a);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("value2:the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerOnlyAllAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        session.save("a5", a);

        CompassHits hits = session.find("value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerController() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer("simple");
        session.save("a6", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        a = new A();
        a.setId(new Long(2));
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer(null);
        try {
            session.save("a6", a);
            fail();
        } catch (SearchEngineException e) {

        }

        tr.commit();
    }

    public void testClassAnalyzerControllerWithNullAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer("simple");
        session.save("a7", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        a = new A();
        a.setId(new Long(2));
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer(null);
        session.save("a7", a);
        hits = session.find("value:the");
        assertEquals(2, hits.getLength());

        tr.commit();
    }
}
