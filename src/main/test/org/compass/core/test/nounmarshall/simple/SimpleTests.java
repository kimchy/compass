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

package org.compass.core.test.nounmarshall.simple;

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
public class SimpleTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"nounmarshall/simple/A.cpm.xml"};
    }

    public void testMappings() {
        ResourceMapping aMapping = ((InternalCompass) getCompass()).getMapping().getRootMappingByAlias("a");
        ResourcePropertyMapping[] aPropertyMappings = aMapping.getResourcePropertyMappings();
        assertEquals(3, aPropertyMappings.length);

        aMapping = ((InternalCompass) getCompass()).getMapping().getRootMappingByAlias("a1");
        aPropertyMappings = aMapping.getResourcePropertyMappings();
        // including the internal id
        assertEquals(4, aPropertyMappings.length);
    }

    public void testNoUnmarshall() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.value2 = "value2";
        session.save("a", a);

        Resource resource = session.loadResource("a", new Long(1));
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("a", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);
        assertEquals("1", resource.getValue("$/a/id"));

        // when unmarshalling, only A with its id is set
        a = (A) session.load("a", new Long(1));
        assertEquals(1, a.id);
        assertNull(a.value);
        assertNull(a.value2);

        tr.commit();
        session.close();
    }

    public void testNoUnmarshallWithIdHasSameMetaDataAsAnotherProperty() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.value2 = "value2";
        session.save("a1", a);

        Resource resource = session.loadResource("a1", new Long(1));
        assertNotNull(resource);
        assertEquals(6, resource.getProperties().length);
        assertEquals("a1", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);
        assertEquals("1", resource.getValue("$/a1/id"));

        // when unmarshalling, only A with its id is set
        a = (A) session.load("a1", new Long(1));
        assertEquals(1, a.id);
        assertNull(a.value);
        assertNull(a.value2);

        tr.commit();
        session.close();
    }
}
