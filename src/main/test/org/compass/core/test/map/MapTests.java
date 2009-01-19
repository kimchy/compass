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

package org.compass.core.test.map;

import java.util.HashMap;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MapTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"map/Map.cpm.xml"};
    }

    protected void addExtraConf(CompassConfiguration conf) {
        MapConverter mapConverter = new MapConverter();
        conf.registerConverter("map", mapConverter);
    }

    public void testMapConverter() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.data = new HashMap();
        a.data.put("entry1", "value1");
        a.data.put("entry2", "value2");

        session.save("a1", a);

        CompassHits hits = session.find("entry1:value1");
        assertEquals(1, hits.length());

        // here we test that it is part of the all property as well
        hits = session.find("value1");
        assertEquals(1, hits.length());

        hits = session.find("value2");
        assertEquals(1, hits.length());

        hits = session.find("entry1:value2");
        assertEquals(0, hits.length());

        a = (A) session.load("a1", new Long(1));
        assertNotNull(a.data);
        assertEquals(2, a.data.size());
        assertEquals("value1", a.data.get("entry1"));
        assertEquals("value2", a.data.get("entry2"));

        tr.commit();
        session.close();
    }
}
