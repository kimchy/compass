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

package org.compass.core.test.component;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ComponentTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/C.cpm.xml", "component/SimpleRoot.cpm.xml",
                "component/SimpleComponent.cpm.xml", "component/id-component.cpm.xml"};
    }

    public void testMappings() {
        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping firstMapping = (ClassMapping) mapping.getRootMappingByClass(CFirst.class);
        String[] propertyNames = firstMapping.getResourcePropertyNames();
        assertEquals(2, propertyNames.length);
        ClassIdPropertyMapping[] idMappings = firstMapping.getClassIdPropertyMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getResourceIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("$/first/id", resourcePropertyMappings[0].getPath().getPath());
        assertEquals("id", resourcePropertyMappings[0].getName());
        ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup("value");
        assertEquals(7, lookup.getResourcePropertyMappings().length);

        ResourcePropertyMapping resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/first/id", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.third.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("$/first/second/third/value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = firstMapping.getResourcePropertyMappingByDotPath("second.third.value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath().getPath());

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
}
