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

package org.compass.core.test.component.cyclic4;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MultiInstanceCyclicTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/cyclic4/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.FILTER_DUPLICATES, true);
    }

    public void testSameIdentityInstance() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Child child = new Child(1, "value");
        Father father = new Father(1, child, child);
        child.father = father;

        session.save("father", father);
        session.save("child", child);

        father = (Father) session.load("father", new Long(1));
        assertSame(father.child1, father.child2);

        Resource resource = session.loadResource("father", new Long(1));
        assertNotNull(resource.getProperty("$/father/id"));
        assertNotNull(resource.getProperty("$/father/child1/id"));
        assertNotNull(resource.getProperty("$/father/child1/value"));
        assertNotNull(resource.getProperty("$/father/child1/father/id"));
        assertNotNull(resource.getProperty("$/father/child2/id"));
        assertNull(resource.getProperty("$/father/child2/value"));
        assertNull(resource.getProperty("$/father/child2/father/id"));

        tr.commit();
        session.close();
    }

    public void testSameEqualsInstance() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Child child1 = new Child(1, "value");
        Child child2 = new Child(1, "value");
        Father father = new Father(1, child1, child2);
        child1.father = father;
        child2.father = father;

        session.save("father", father);
        session.save("child", child1);
        session.save("child", child2);

        father = (Father) session.load("father", new Long(1));
        assertSame(father.child1, father.child2);
        assertSame(father, father.child1.father);
        assertSame(father, father.child2.father);

        Resource resource = session.loadResource("father", new Long(1));
        assertNotNull(resource.getProperty("$/father/id"));
        assertNotNull(resource.getProperty("$/father/child1/id"));
        assertNotNull(resource.getProperty("$/father/child1/value"));
        assertNotNull(resource.getProperty("$/father/child1/father/id"));
        assertNotNull(resource.getProperty("$/father/child2/id"));
        assertNull(resource.getProperty("$/father/child2/value"));
        assertNull(resource.getProperty("$/father/child2/father/id"));

        tr.commit();
        session.close();
    }

    public void testIdentityCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Child child1 = new Child(1, "value1");
        Child child2 = new Child(2, "value2");
        Father father = new Father(1);
        father.children.add(child1);
        father.children.add(child2);
        father.children.add(child1);
        child1.father = father;
        child2.father = father;

        session.save("father", father);
        session.save("child", child1);
        session.save("child", child2);

        father = (Father) session.load("father", new Long(1));
        assertEquals(3, father.children.size());
        assertSame(father.children.get(0), father.children.get(2));
        assertNotSame(father.children.get(1), father.children.get(2));
        assertSame(father, ((Child)father.children.get(0)).father);
        assertSame(father, ((Child)father.children.get(1)).father);
        assertSame(father, ((Child)father.children.get(2)).father);

        Resource resource = session.loadResource("father", new Long(1));
        assertNotNull(resource.getProperty("$/father/id"));
        assertEquals(3, resource.getProperties("$/father/children/id").length);
        assertEquals(2, resource.getProperties("$/father/children/value").length);
        assertEquals(2, resource.getProperties("value").length);
        // only two here, since we have duplicate childs
        assertEquals(2, resource.getProperties("$/father/children/father/id").length);

        tr.commit();
        session.close();
    }
}
