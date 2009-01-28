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

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("a");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("a.value:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        // this one will use the simple analyzer
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

    }

    public void testResourceAnalyzerSetForResource() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("b");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("b.value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerSetForResourceWithCompassQuery() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("b");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        CompassHits hits = session.find("b.value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.length());

        // this won't take into account without forcing the analyzer
        CompassQuery query = session.queryBuilder().queryString("b.value:the").setAnalyzer("default").toQuery();
        assertEquals(1, query.hits().getLength());

        query = session.queryBuilder().queryString("b.value:the").setAnalyzer("default").forceAnalyzer().toQuery();
        assertEquals(0, query.hits().getLength());

        query = session.queryBuilder().queryString("b.value:the").setAnalyzer("simple").forceAnalyzer().toQuery();
        assertEquals(1, query.hits().length());

        tr.commit();
    }

    public void testResourceAnalyzerSetForProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("d");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("value2", TEXT);
        session.save(r);

        CompassHits hits = session.find("d.value:the");
        assertEquals(1, hits.getLength());
        hits = session.find("d.value2:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerSetForResourceAndProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("e");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("value2", TEXT);
        session.save(r);

        CompassHits hits = session.find("e.value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("e.value2:the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerController() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.find("g.value:the");
        assertEquals(1, hits.getLength());

        r = getCompass().getResourceFactory().createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        try {
            session.save(r);
            tr.commit();
            fail();
        } catch (SearchEngineException e) {
            tr.rollback();
        }

        session.close();
    }

    public void testResourceAnalyzerControllerWithNullAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("g");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.find("g.value:the");
        assertEquals(1, hits.getLength());

        r = getCompass().getResourceFactory().createResource("h");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        session.save(r);

        // analyzer controller can't affect query string (since we don't have the resource), just for simple and
        // check that both h and i were saved
        hits = session.queryBuilder().queryString("value:the").setAnalyzer("simple").forceAnalyzer().toQuery().hits();
        assertEquals(2, hits.getLength());

        tr.commit();
    }

    public void testResourceAnalyzerControllerWithNullAnalyzerAndPropertyAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        Resource r = getCompass().getResourceFactory().createResource("i");
        r.addProperty("id", id);
        r.addProperty("value", TEXT);
        r.addProperty("analyzer", "simple");
        session.save(r);

        CompassHits hits = session.queryBuilder().queryString("value:the").setAnalyzer("simple").forceAnalyzer().toQuery().hits();
        assertEquals(0, hits.getLength());

        tr.commit();
    }
}
