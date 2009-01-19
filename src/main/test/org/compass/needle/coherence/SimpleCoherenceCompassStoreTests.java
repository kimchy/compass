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

package org.compass.needle.coherence;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;

/**
 * @author kimchy
 */
public class SimpleCoherenceCompassStoreTests extends TestCase {

    public void testSimpleInvocableStore() {
        CompassConfiguration conf = CompassConfigurationFactory.newConfiguration().setConnection("coherence://test:lucene")
                .addClass(A.class);
        Compass compass = conf.buildCompass();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "this is a test";
        session.save(a);

        tr.commit();
        session.close();

        session = compass.openSession();
        tr = session.beginTransaction();

        CompassHits hits = session.find("test");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();

    }

    public void testSimpleSGStore() {
        CompassConfiguration conf = CompassConfigurationFactory.newConfiguration().setConnection("coherence-dg://test:lucene")
                .addClass(A.class);
        Compass compass = conf.buildCompass();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "this is a test";
        session.save(a);

        tr.commit();
        session.close();

        session = compass.openSession();
        tr = session.beginTransaction();

        CompassHits hits = session.find("test");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();

    }
}