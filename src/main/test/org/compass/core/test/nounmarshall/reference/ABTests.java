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

package org.compass.core.test.nounmarshall.reference;

import java.util.ArrayList;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ABTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nounmarshall/reference/AB.cpm.xml"};
    }

    public void testMappings() {
        ResourceMapping aMapping = ((InternalCompass) getCompass()).getMapping().getRootMappingByAlias("a");
        ResourcePropertyMapping[] aPropertyMappings = aMapping.getResourcePropertyMappings();
        assertEquals(4, aPropertyMappings.length);
        assertNotNull(aMapping.getResourcePropertyMapping("value"));
        assertNotNull(aMapping.getResourcePropertyMapping("id"));
    }

    public void testBSingleValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "value";
        B b = new B();
        b.id = new Long(1);
        b.value = "bvalue";
        a.b = b;
        session.save(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, new Long(1));
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(1, resource.getProperties("value").length);
        assertEquals("1", resource.getValue("$/a/b/id"));

        a = (A) session.load(A.class, new Long(1));
        assertEquals(1, a.id.longValue());
        assertNull(a.value);
        assertNull(a.b);

        tr.commit();
        session.close();
    }

    public void testBTwoValues() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "value";
        B b = new B();
        b.id = new Long(1);
        b.value = "bvalue";
        b.value2 = "bvalue2";
        a.b = b;
        session.save(a);

        Resource resource = session.loadResource(A.class, new Long(1));
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(1, resource.getProperties("value").length);
        assertEquals("1", resource.getValue("$/a/b/id"));

        a = (A) session.load(A.class, new Long(1));
        assertEquals(1, a.id.longValue());
        assertNull(a.value);
        assertNull(a.b);

        tr.commit();
        session.close();
    }

    public void testBCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "value";
        a.bs = new ArrayList();
        B b = new B();
        b.id = new Long(1);
        b.value = "bvalue11";
        b.value2 = "bvalue12";
        session.save(b);
        a.bs.add(b);
        b = new B();
        b.id = new Long(2);
        b.value = "bvalue21";
        b.value = "bvalue22";
        session.save(b);
        a.bs.add(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, new Long(1));
        assertNotNull(resource);
        assertEquals(6, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(1, resource.getProperties("value").length);
        assertEquals(2, resource.getProperties("$/a/bs/id").length);

        tr.commit();
        session.close();
    }
}
