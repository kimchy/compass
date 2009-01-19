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

package org.compass.core.test.component.inheritance3;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * This test verifies that with polymorphic relationship, with the
 * base class having a colleciton of properties, all is well and no
 * internal id is generated for the
 * {@link org.compass.core.test.component.inheritance3.Child#names}
 * mapping.
 *
 * @author kimchy
 */
public class Ineritance3Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/inheritance3/mapping.cpm.xml"};
    }

    public void testCorrectInternalId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Father father = new Father();
        father.id = "1";
        father.child = new Child();
        father.child.names = new String[] {"value1", "value2"};
        session.save(father);

        Resource resource = session.loadResource(Father.class, "1");
        assertEquals(7, resource.getProperties().length);
        assertEquals("father", resource.getValue("alias"));
        assertEquals("1", resource.getValue("$/father/id"));
        assertEquals(2, resource.getProperties("name").length);
        assertEquals(Child.class.getName(), resource.getValue("$/father/child/class"));
        assertEquals("2", resource.getValue("$/father/child/names/colSize"));

        father = (Father) session.load(Father.class, "1");
        assertEquals(2, father.child.names.length);
        assertEquals("value1", father.child.names[0]);
        assertEquals("value2", father.child.names[1]);

        tr.commit();
        session.close();
    }
}
