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
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class OsemAnalyzerTests extends AbstractAnalyzerTests {

    protected String[] getMappings() {
        return new String[]{"analyzer/osem.cpm.xml"};
    }

    public void testClassNoAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        session.save("a1", a);

        CompassHits hits = session.find("a1.value:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(0, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForResource() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        session.save("a2", a);

        CompassHits hits = session.find("a2.value:the");
        assertEquals(1, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        session.save("a3", a);

        CompassHits hits = session.find("a3.value:the");
        assertEquals(1, hits.getLength());
        hits = session.find("a3.value2:the");
        assertEquals(0, hits.getLength());
        // test for the all property as well
        hits = session.find("the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerSetForResourceAndProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        session.save("a4", a);

        CompassHits hits = session.find("a4.value:the");
        assertEquals(0, hits.getLength());
        hits = session.find("a4.value2:the");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testClassAnalyzerController() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer("simple");
        session.save("a6", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        a = new A();
        a.setId((long) 2);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer(null);
        try {
            session.save("a6", a);
            tr.commit();
            fail();
        } catch (SearchEngineException e) {
            tr.rollback();
        }
        session.close();
    }

    public void testClassAnalyzerControllerWithNullAnalyzer() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = (long) 1;
        A a = new A();
        a.setId(id);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer("simple");
        session.save("a7", a);

        CompassHits hits = session.find("value:the");
        assertEquals(1, hits.getLength());

        a = new A();
        a.setId((long) 2);
        a.setValue(TEXT);
        a.setValue2(TEXT);
        a.setAnalyzer(null);
        session.save("a7", a);
        hits = session.find("value:the");
        assertEquals(2, hits.getLength());

        tr.commit();
    }
}
