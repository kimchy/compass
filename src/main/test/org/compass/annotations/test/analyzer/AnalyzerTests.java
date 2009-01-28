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
            tr.commit();
            fail();
        } catch (SearchEngineException e) {

        }
        session.rollback();
    }

}
