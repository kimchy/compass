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

package org.compass.core.test.nounmarshall.component.cyclic2;

import java.util.HashSet;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class Cyclic2Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nounmarshall/component/cyclic2/mapping.cpm.xml"};
    }

    public void testMappings() {
        ResourceMapping aMapping = ((InternalCompass) getCompass()).getMapping().getRootMappingByAlias("a");
        ResourcePropertyMapping[] aPropertyMappings = aMapping.getResourcePropertyMappings();
        assertEquals(5, aPropertyMappings.length);
    }

    public void testSimpleCyclicNoUnmarshall() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "avalue";

        B b = new B();
        b.value = "bvalue";

        C c = new C();
        c.value = "cvalue";

        D d = new D();
        d.value = "dvalue";

        a.b = b;
        b.c = c;
        b.d = d;
        c.a = a;
        d.b = b;

        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        // 5 mappings + uid + alias
        assertEquals(7, resource.getProperties().length);
        HashSet<String> values = new HashSet<String>();
        for (Property prop : resource.getProperties("value")) {
            values.add(prop.getStringValue());
        }
        assertEquals(4, values.size());
        assertTrue(values.contains("avalue"));
        assertTrue(values.contains("bvalue"));
        assertTrue(values.contains("cvalue"));
        assertTrue(values.contains("dvalue"));

        assertNotNull(resource.getValue("$/a/id"));
        assertNotNull(resource.getValue("$/uid"));

        tr.commit();
        session.close();
    }
}
