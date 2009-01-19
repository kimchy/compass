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

package org.compass.core.test.property.exclude;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ExcludeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/exclude/mapping.cpm.xml"};
    }

    public void testExcludeNoPropertyMapping() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a  = new A("1", "value1", "value2");
        session.save("a1", a);

        a = (A) session.load("a1", "1");
        assertEquals("1", a.id);
        // value 1 is null since we have no property mapping for it
        assertNull(a.value1);
        assertEquals("value2", a.value2);

        // since value 1 has no property mapping, it should not be
        // found in all
        CompassHits hits = session.find("value1");
        assertEquals(0, hits.length());

        hits = session.find("value2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testExcludePropertyMappingNoMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a  = new A("1", "value1", "value2");
        session.save("a2", a);

        a = (A) session.load("a2", "1");
        assertEquals("1", a.id);
        assertEquals("value1", a.value1);
        assertEquals("value2", a.value2);

        // since value 1 has no property mapping, it should not be
        // found in all
        CompassHits hits = session.find("value1");
        assertEquals(0, hits.length());

        hits = session.find("value2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
