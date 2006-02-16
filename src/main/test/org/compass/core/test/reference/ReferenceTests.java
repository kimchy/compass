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

package org.compass.core.test.reference;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyIdMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReferenceTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "reference/Reference.cpm.xml" };
    }
    
    public void testMappings() {
        
        InternalCompassSession session = (InternalCompassSession) openSession();
        
        CompassMapping mapping = session.getMapping();
        ClassMapping xMapping = (ClassMapping) mapping.getRootMappingByAlias("x");
        ClassMapping yMapping = ((ReferenceMapping) xMapping.getMapping("y")).getRefClassMapping();
        ClassPropertyIdMapping[] idMappings = yMapping.getClassPropertyIdMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("$/x/y/id", resourcePropertyMappings[0].getPath());
        assertEquals("id", resourcePropertyMappings[0].getName());

        ResourcePropertyMapping resourcePropertyMapping = xMapping.getMappingByPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/x/id", resourcePropertyMapping.getPath());

        resourcePropertyMapping = xMapping.getMappingByPath("value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = xMapping.getMappingByPath("value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath());

        resourcePropertyMapping = xMapping.getMappingByPath("y.id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/x/y/id", resourcePropertyMapping.getPath());
        
        session.close();
    }

    public void testXY() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long xId = new Long(1);
        Long yId = new Long(2);
        X x = new X();
        x.setId(xId);
        x.setValue("xValue");
        Y y = new Y();
        y.setId(yId);
        y.setValue("yValue");
        x.setY(y);
        session.save("y", y);
        session.save("x", x);

        x = (X) session.load("x", xId);
        assertEquals("xValue", x.getValue());
        assertNotNull(x.getY());
        assertEquals("yValue", x.getY().getValue());

        CompassHits hits = session.queryBuilder().term("x.y.id", new Long(2)).hits();
        assertEquals(1, hits.length());

        hits = session.queryBuilder().term("x.id", new Long(1)).hits();
        assertEquals(1, hits.length());

        tr.commit();
    }

    public void testXYRefCompMapping() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long xId = new Long(1);
        Long yId = new Long(2);
        X x = new X();
        x.setId(xId);
        x.setValue("xValue");
        Y y = new Y();
        y.setId(yId);
        y.setValue("yValue");
        x.setY(y);
        session.save("y1", y);
        session.save("x1", x);

        x = (X) session.load("x1", xId);
        assertEquals("xValue", x.getValue());
        assertNotNull(x.getY());
        assertEquals("yValue", x.getY().getValue());

        CompassHits hits = session.queryBuilder().term("x1.y.id", new Long(2)).hits();
        assertEquals(1, hits.length());

        hits = session.queryBuilder().term("x1.id", new Long(1)).hits();
        assertEquals(1, hits.length());

        hits = session.queryBuilder().term("value1", "yvalue").hits();
        assertEquals(1, hits.length());
        x = (X) hits.data(0);
        assertEquals("xValue", x.getValue());
        assertNotNull(x.getY());
        assertEquals("yValue", x.getY().getValue());

        hits = session.queryBuilder().term("x1.y.value.value1", "yvalue").hits();
        assertEquals(1, hits.length());

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
        cyclic2.setId(id);
        cyclic2.setValue("cyclic2");

        cyclic1.setCyclic2(cyclic2);
        cyclic2.setCyclic1(cyclic1);

        session.save(cyclic2);
        session.save(cyclic1);

        cyclic1 = (Cyclic1) session.load(Cyclic1.class, id);
        assertNotNull(cyclic1.getCyclic2());
        assertEquals("cyclic2", cyclic1.getCyclic2().getValue());
        cyclic2 = cyclic1.getCyclic2();
        assertNotNull(cyclic2);
        assertEquals("cyclic1", cyclic2.getCyclic1().getValue());

        cyclic2 = (Cyclic2) session.load(Cyclic2.class, id);
        assertNotNull(cyclic2.getCyclic1());
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

}
