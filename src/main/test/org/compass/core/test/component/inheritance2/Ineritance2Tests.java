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

package org.compass.core.test.component.inheritance2;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * This test verifies that with ploymprphic relationship, on the component
 * of the component mapping, things works. Also verifies that no intenral
 * id is created for {@link org.compass.core.test.component.inheritance2.GrandChild#value}.
 *
 * @author kimchy
 */
public class Ineritance2Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/inheritance2/mapping.cpm.xml"};
    }

    public void testCorrectInternalId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Father father = new Father();
        father.id = "1";
        father.child = new Child();
        father.child.child = new GrandChild1();
        father.child.child.value = "value";
        session.save(father);

        Resource resource = session.loadResource(Father.class, "1");
        assertEquals(5, resource.getProperties().length);
        assertEquals("father", resource.getValue("alias"));
        assertEquals("1", resource.getValue("$/father/id"));
        assertEquals("value", resource.getValue("value"));
        assertEquals(GrandChild1.class.getName(), resource.getValue("$/father/child/child/class"));

        father = (Father) session.load(Father.class, "1");
        assertEquals("1", father.id);
        assertEquals("value", father.child.child.value);

        tr.commit();
        session.close();
    }
}
