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

package org.compass.core.test.nounmarshall.component.simple2;

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
public class SimpleNoUnmarshallComonentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nounmarshall/component/simple2/mapping.cpm.xml"};
    }

    public void testMappingsA() {
        ResourceMapping aMapping = ((InternalCompass) getCompass()).getMapping().getRootMappingByAlias("a");
        ResourcePropertyMapping[] aPropertyMappings = aMapping.getResourcePropertyMappings();
        assertEquals(5, aPropertyMappings.length);
        assertNotNull(aMapping.getResourcePropertyMapping("value"));
        assertNotNull(aMapping.getResourcePropertyMapping("value2"));
        assertNotNull(aMapping.getResourcePropertyMapping("id"));
    }

    public void testBSingleValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        a.b = b;
        session.save("a", a);

        Resource resource = session.loadResource("a", new Long(1));
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);

        // make sure no unmarshall returns A only with its ids set
        a = (A) session.load("a", 1);
        assertEquals(1, a.id);
        assertNull(a.b);

        tr.commit();
        session.close();
    }

    public void testBwoValues() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        b.value2 = "bvalue2";
        a.b = b;
        session.save("a", a);

        Resource resource = session.loadResource("a", new Long(1));
        assertNotNull(resource);
        assertEquals(7, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);

        // make sure no unmarshall returns A only with its ids set
        a = (A) session.load("a", 1);
        assertEquals(1, a.id);
        assertNull(a.b);

        tr.commit();
        session.close();
    }
}