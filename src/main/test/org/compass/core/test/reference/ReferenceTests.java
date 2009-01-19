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

package org.compass.core.test.reference;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReferenceTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"reference/Reference.cpm.xml"};
    }

    public void testMappings() {

        InternalCompassSession session = (InternalCompassSession) openSession();

        CompassMapping mapping = session.getMapping();
        ClassMapping xMapping = (ClassMapping) mapping.getRootMappingByAlias("x");
        ClassMapping yMapping = ((ReferenceMapping) xMapping.getMapping("y")).getRefClassMappings()[0];
        ClassIdPropertyMapping[] idMappings = yMapping.getClassIdPropertyMappings();
        assertEquals(1, idMappings.length);
        ResourcePropertyMapping[] resourcePropertyMappings = idMappings[0].getResourceIdMappings();
        assertEquals(1, resourcePropertyMappings.length);
        assertEquals("$/x/y/id", resourcePropertyMappings[0].getPath().getPath());
        assertEquals("id", resourcePropertyMappings[0].getName());

        ResourcePropertyMapping resourcePropertyMapping = xMapping.getResourcePropertyMappingByDotPath("id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/x/id", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = xMapping.getResourcePropertyMappingByDotPath("value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = xMapping.getResourcePropertyMappingByDotPath("value.value");
        assertNotNull(resourcePropertyMapping);
        assertEquals("value", resourcePropertyMapping.getName());
        assertEquals("value", resourcePropertyMapping.getPath().getPath());

        resourcePropertyMapping = xMapping.getResourcePropertyMappingByDotPath("y.id");
        assertNotNull(resourcePropertyMapping);
        assertEquals("id", resourcePropertyMapping.getName());
        assertEquals("$/x/y/id", resourcePropertyMapping.getPath().getPath());

        session.close();
    }

    public void testInferMappings() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue("avalue");
        B b = new B();
        b.setId(new Long(1));
        b.setValue("bvalue");
        a.setB(b);

        session.save("b", b);
        session.save("a", a);

        a = (A) session.load("a", new Long(1));
        assertEquals("avalue", a.getValue());
        assertEquals("bvalue", a.getB().getValue());

        tr.commit();
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
