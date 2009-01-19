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

package org.compass.annotations.test.converter;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.annotations.test.Converted;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ConverterTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class).addPackage("org.compass.annotations.test.converter");
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Converted("id1", "id2");
        a.value = new Converted("value1", "value2");

        session.save(a);

        a = session.load(A.class, a.id);
        assertEquals("id1", a.id.value1);
        assertEquals("id2", a.id.value2);
        assertEquals("value1", a.value.value1);
        assertEquals("value2", a.value.value2);

        Resource resource = session.loadResource(A.class, a.id);
        assertEquals("id1#id2", resource.getValue("$/A/id"));
        assertEquals("value1#value2", resource.getValue("value"));

        CompassHits hits = session.find("value1#value2");
        assertEquals(1, hits.length());


        tr.commit();
        session.close();
    }

    public void testDoubleConverterBetween() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = 1;
        b.value = 1001.456;
        session.save(b);
        b.id = 2;
        b.value = 1594;
        session.save(b);

        CompassHits hits = session.queryBuilder().between("B.value", 1000.0, 2000.0, true).hits();
        assertEquals(2, hits.length());

        assertEquals(Property.Index.NOT_ANALYZED,
                getCompass().getMapping().getResourcePropertyLookup("B.value.value").getResourcePropertyMapping().getIndex());

        tr.commit();
        session.close();
    }
}
