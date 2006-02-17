/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.test.component;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ComponentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/C.cpm.xml", "component/SimpleRoot.cpm.xml",
                "component/SimpleComponent.cpm.xml", "component/id-component.cpm.xml", "component/Cyclic.cpm.xml",
                "component/SelfCycle.cpm.xml"};
    }

    public void testMappings() {
        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping firstMapping = (ClassMapping) mapping.getRootMappingByClass(CFirst.class);
        ClassIdPropertyMapping[] idMappings = firstMapping.getClassPropertyIdMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("$/first/id", resourcePropertyMappings[0].getPath());
        assertEquals("id", resourcePropertyMappings[0].getName());

        ResourcePropertyMapping resourcePropertyMapping = firstMapping.getMappingByPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/first/id", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("second.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("second.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("second.third.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/third/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getMappingByPath("second.third.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        session.close();
    }

    public void testRoot() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);

        SimpleRoot root = new SimpleRoot();
        root.setId(id);
        root.setValue("test");
        SimpleComponent first = new SimpleComponent();
        first.setValue("test1");
        root.setFirstComponent(first);
        SimpleComponent second = new SimpleComponent();
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("sr", root);

        root = (SimpleRoot) session.load("sr", id);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        tr.commit();
    }

    public void testIdComp() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);

        SimpleRoot root = new SimpleRoot();
        root.setId(id);
        root.setValue("test");
        SimpleComponent first = new SimpleComponent();
        first.setId(new Long(1));
        first.setValue("test1");
        root.setFirstComponent(first);
        SimpleComponent second = new SimpleComponent();
        second.setId(new Long(2));
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("id-sr", root);

        root = new SimpleRoot();
        root.setId(new Long(2));
        root.setValue("test");
        first = new SimpleComponent();
        first.setId(new Long(3));
        first.setValue("test1");
        root.setFirstComponent(first);
        second = new SimpleComponent();
        second.setId(new Long(4));
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("id-sr", root);

        root = (SimpleRoot) session.load("id-sr", id);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        CompassHits hits = session.find("id-sc:2");
        assertEquals(1, hits.getLength());
        root = (SimpleRoot) hits.data(0);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        tr.commit();
    }

    public void testCXNull() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);

        CFirst first = new CFirst();
        first.setId(id);
        first.setValue("test");
        CSecond second = new CSecond();
        second.setValue("test1");
        first.setSecond(second);
        session.save(first);

        first = (CFirst) session.load(CFirst.class, id);
        assertEquals("test", first.getValue());
        assertNotNull(first.getSecond());
        assertEquals("test1", first.getSecond().getValue());
        assertNull(first.getSecond().getThird());

        tr.commit();
    }

    public void testCX() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);

        CFirst first = new CFirst();
        first.setId(id);
        first.setValue("test");
        CSecond second = new CSecond();
        second.setValue("test1");
        CThird third = new CThird();
        third.setValue("test2");
        CFourth fourth = new CFourth();
        fourth.setValue("test3");
        third.setFourth(fourth);
        second.setThird(third);
        first.setSecond(second);
        session.save(first);

        first = (CFirst) session.load(CFirst.class, id);
        assertEquals("test", first.getValue());
        assertNotNull(first.getSecond());
        assertEquals("test1", first.getSecond().getValue());
        assertNotNull(first.getSecond().getThird());
        assertEquals("test2", first.getSecond().getThird().getValue());
        assertNotNull(first.getSecond().getThird().getFourth());
        assertEquals("test3", first.getSecond().getThird().getFourth().getValue());

        CompassHits hits = session.find("test");
        assertEquals(1, hits.getLength());
        hits = session.find("test1");
        assertEquals(1, hits.getLength());
        hits = session.find("test2");
        assertEquals(1, hits.getLength());
        hits = session.find("test3");
        assertEquals(1, hits.getLength());

        tr.commit();
    }

    public void testCyclic() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.setId(id);
        cyclic1.setValue("cyclic1");

        Cyclic2 cyclic2 = new Cyclic2();
        cyclic2.setValue("cyclic2");

        cyclic1.setCyclic2(cyclic2);
        cyclic2.setCyclic1(cyclic1);

        session.save(cyclic1);

        cyclic1 = (Cyclic1) session.load(Cyclic1.class, id);
        assertNotNull(cyclic1.getCyclic2());
        assertEquals("cyclic2", cyclic1.getCyclic2().getValue());
        cyclic2 = cyclic1.getCyclic2();
        assertNotNull(cyclic2);
        assertEquals("cyclic1", cyclic2.getCyclic1().getValue());

        tr.commit();
        session.close();
    }

    public void testCyclicNull() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.setId(id);
        cyclic1.setValue("cyclic1");

        session.save(cyclic1);

        cyclic1 = (Cyclic1) session.load(Cyclic1.class, id);
        assertNull(cyclic1.getCyclic2());

        tr.commit();
        session.close();
    }

    public void testSelfCyclic() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        SelfCycle selfCycle1 = new SelfCycle();
        selfCycle1.setId(new Long(1));
        selfCycle1.setValue("value1");

        SelfCycle selfCycle2 = new SelfCycle();
        selfCycle2.setValue("value2");

        SelfCycle selfCycle3 = new SelfCycle();
        selfCycle3.setValue("value3");

        SelfCycle selfCycle4 = new SelfCycle();
        selfCycle4.setValue("value4");

        SelfCycle selfCycle5 = new SelfCycle();
        selfCycle5.setValue("value5");

        selfCycle1.setSelfCycle(selfCycle2);
        selfCycle2.setSelfCycle(selfCycle3);
        selfCycle3.setSelfCycle(selfCycle4);
        selfCycle4.setSelfCycle(selfCycle5);

        session.save(selfCycle1);

        selfCycle1 = (SelfCycle) session.load(SelfCycle.class, new Long(1));
        assertEquals("value1", selfCycle1.getValue());
        assertEquals("value2", selfCycle1.getSelfCycle().getValue());
        assertEquals("value3", selfCycle1.getSelfCycle().getSelfCycle().getValue());
        assertEquals("value4", selfCycle1.getSelfCycle().getSelfCycle().getSelfCycle().getValue());
        assertNull(selfCycle1.getSelfCycle().getSelfCycle().getSelfCycle().getSelfCycle());

        tr.commit();
        session.close();
    }
}
