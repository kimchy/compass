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

package org.compass.core.test.reverse;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReverseTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"reverse/reverse.cpm.xml"};
    }

    public void testReverseAndInternalIdAnString() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("12345");

        session.save("a1", a);

        Resource resource = session.loadResource("a1", id);
        assertNotNull(resource.getProperty("$/a1/value"));
        assertEquals("12345", resource.getProperty("$/a1/value").getStringValue());
        assertEquals("54321", resource.getProperty("value").getStringValue());

        a = (A) session.load("a1", id);
        assertEquals("12345", a.getValue());

        CompassHits hits = session.find("54*");
        assertEquals(1, hits.length());

        hits = session.find("value:54*");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();

    }

    public void testReverseReader() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("12345");

        session.save("a2", a);

        a = (A) session.load("a2", id);
        assertEquals("12345", a.getValue());

        session.loadResource("a2", id);

        CompassHits hits = session.find("valuerev:54*");
        assertEquals(1, hits.length());

        hits = session.find("54321");
        assertEquals(1, hits.length());

        hits = session.find("54*");
        assertEquals(1, hits.length());

        hits = session.find("value:12*");
        assertEquals(1, hits.length());

        hits = session.find("12*");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
