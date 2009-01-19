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

package org.compass.annotations.test.inheritance;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.ResourceMapping;

/**
 * @author kimchy
 */
public class InheritanceTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
        conf.addClass(B.class);
        conf.addClass(C.class);
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.All.EXCLUDE_ALIAS, false);
    }

    public void testExtendedAliases() {
        ResourceMapping resourceMapping = getCompass().getMapping().getMappingByAlias("b");
        assertEquals(1, resourceMapping.getExtendedAliases().length);
        assertEquals("a1", resourceMapping.getExtendedAliases()[0]);

        resourceMapping = getCompass().getMapping().getMappingByAlias("a1");
        assertEquals(0, resourceMapping.getExtendedAliases().length);
    }

    public void testExtendingAliases() {
        ResourceMapping resourceMapping = getCompass().getMapping().getMappingByAlias("a1");
        assertEquals(1, resourceMapping.getExtendingAliases().length);
        assertEquals("b", resourceMapping.getExtendingAliases()[0]);

        resourceMapping = getCompass().getMapping().getMappingByAlias("b");
        assertEquals(0, resourceMapping.getExtendingAliases().length);
    }

    public void testOverride() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.setId(1);
        b.setValue1("value1");
        b.setValue2("value2");
        session.save(b);

        b = session.load(B.class, 1);
        assertEquals("value1", b.getValue1());
        assertEquals("value2", b.getValue2());

        Resource resource = session.loadResource(B.class, 1);
        // 5 properties, one for the alias, and one for the poly class
        assertEquals(8, resource.getProperties().length);
        assertNull(resource.getValue("value1"));
        assertNotNull(resource.getValue("value1e"));
        assertNotNull(resource.getValue("value2"));
        assertNotNull(resource.getValue("value2e"));
        assertEquals(resource.getValue("abase"), "abasevalue");

        tr.commit();
        session.close();
    }

    public void testComponentRefAliasInheritance() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.setId(1);
        b.setValue1("value1");
        b.setValue2("value2");
        b.setValue3("value3");

        C c = new C();
        c.id = 1;
        c.a = b;
        session.save(c);
        c = session.load(C.class, 1);
        assertTrue(c.a instanceof B);
        assertEquals("value1", c.a.getValue1());
        assertEquals("value2", c.a.getValue2());
        assertEquals("value3", ((B) c.a).getValue3());

        Resource resource = session.loadResource(C.class, 1);
        assertNull(resource.getValue("value1"));
        assertNotNull(resource.getValue("value1e"));
        assertNotNull(resource.getValue("value2"));
        assertNotNull(resource.getValue("value2e"));
        assertEquals(resource.getValue("abase"), "abasevalue");

        tr.commit();
        session.close();
    }

    public void testPolyAliasQuery() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.setId(1);
        b.setValue1("value1");
        b.setValue2("value2");
        session.save(b);

        CompassHits hits = session.queryBuilder().alias("b").hits();
        assertEquals(1, hits.length());
        hits = session.queryBuilder().polyAlias("a1").hits();
        assertEquals(1, hits.length());
        hits = session.queryBuilder().polyAlias("b").hits();
        assertEquals(1, hits.length());
        hits = session.queryBuilder().alias("a1").hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testAllExtendedAliases() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.setId(1);
        b.setValue1("value1");
        b.setValue2("value2");
        session.save(b);

        assertEquals(1, session.find("abasevalue").length());
        assertEquals(1, session.find("b").length());
        assertEquals(1, session.find("a1").length());

        tr.commit();
        session.close();
    }
}
