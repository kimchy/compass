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
import org.compass.core.spi.InternalCompassSession;
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
                "component/SelfCycle.cpm.xml", "component/InferRefAlias.cpm.xml", "component/ManyToMany.cpm.xml"};
    }

    public void testMappings() {
        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping firstMapping = (ClassMapping) mapping.getRootMappingByClass(CFirst.class);
        String[] propertyNames = firstMapping.getResourcePropertyNames();
        assertEquals(2, propertyNames.length);
        ClassIdPropertyMapping[] idMappings = firstMapping.getClassPropertyIdMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("$/first/id", resourcePropertyMappings[0].getPath());
        assertEquals("id", resourcePropertyMappings[0].getName());

        ResourcePropertyMapping resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/first/id", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.third.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/third/value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.third.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        session.close();
    }

    public void testInferRefAlias() {
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
        session.save("sr-infer", root);

        root = (SimpleRoot) session.load("sr-infer", id);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        tr.commit();
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

        SimpleRootId root = new SimpleRootId();
        root.setId(id);
        root.setValue("test");
        SimpleComponentId first = new SimpleComponentId();
        first.setId(new Long(1));
        first.setValue("test1");
        root.setFirstComponent(first);
        SimpleComponentId second = new SimpleComponentId();
        second.setId(new Long(2));
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("id-sr", root);

        root = new SimpleRootId();
        root.setId(new Long(2));
        root.setValue("test");
        first = new SimpleComponentId();
        first.setId(new Long(3));
        first.setValue("test1");
        root.setFirstComponent(first);
        second = new SimpleComponentId();
        second.setId(new Long(4));
        second.setValue("test2");
        root.setSecondComponent(second);
        session.save("id-sr", root);

        root = (SimpleRootId) session.load("id-sr", id);
        assertEquals("test", root.getValue());
        assertNotNull(root.getFirstComponent());
        assertEquals("test1", root.getFirstComponent().getValue());
        assertNotNull(root.getSecondComponent());
        assertEquals("test2", root.getSecondComponent().getValue());

        CompassHits hits = session.find("id-sc:2");
        assertEquals(1, hits.getLength());
        root = (SimpleRootId) hits.data(0);
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

    public void testManyToMany() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        ManyToMany1 many11 = new ManyToMany1();
        many11.id = new Long(1);
        many11.value = "many11";

        ManyToMany1 many12 = new ManyToMany1();
        many12.id = new Long(2);
        many12.value = "many12";

        ManyToMany2 many21 = new ManyToMany2();
        many21.id = new Long(1);
        many21.value = "many21";

        many11.many2.add(many21);
        many12.many2.add(many21);

        many21.many1.add(many11);
        many21.many1.add(many12);

        session.save(many11);
        session.save(many12);
        session.save(many21);

        many21 = (ManyToMany2) session.load("many2", new Long(1));
        assertEquals("many21", many21.value);
        assertEquals(2, many21.many1.size());
        assertEquals("many11", ((ManyToMany1) many21.many1.get(0)).value);
        assertEquals("many12", ((ManyToMany1) many21.many1.get(1)).value);

        many11 = (ManyToMany1) session.load("many1", new Long(1));
        assertEquals("many11", many11.value);
        assertEquals(1, many11.many2.size());
        assertEquals("many21", ((ManyToMany2) many11.many2.get(0)).value);

        tr.commit();
        session.close();
    }
}
