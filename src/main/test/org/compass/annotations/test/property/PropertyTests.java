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

package org.compass.annotations.test.property;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.annotations.test.Converted;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class PropertyTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        ArrayList<String> values = new ArrayList<String>();
        values.add("test1");
        values.add("test2");
        a.values = values;

        session.save(a);

        a = session.load(A.class, 1);
        assertEquals(values, a.values);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        CompassHits hits = session.find("test1");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);
        hits = session.find("test2");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        tr.commit();
        session.close();
    }

    public void testConvertedProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id  = 1;
        b.converted1 = new Converted("value1", "value2");
        b.converted2 = new Converted("value3", "value4");

        ArrayList<Converted> convertedValues = new ArrayList<Converted>();
        convertedValues.add(new Converted("value11", "value12"));
        convertedValues.add(new Converted("value21", "value22"));

        b.convertedValues = convertedValues;
        session.save(b);

        b = session.load(B.class, 1);
        assertEquals("value1", b.converted1.value1);
        assertEquals("value2", b.converted1.value2);
        assertEquals("value3", b.converted2.value1);
        assertEquals("value4", b.converted2.value2);

        assertEquals("value11", b.convertedValues.get(0).value1);
        assertEquals("value12", b.convertedValues.get(0).value2);
        assertEquals("value21", b.convertedValues.get(1).value1);
        assertEquals("value22", b.convertedValues.get(1).value2);

        CompassHits hits = session.find("value1/value2");
        assertEquals(1, hits.length());

        hits = session.find("value11/value12");
        assertEquals(1, hits.length());

        hits = session.find("value21/value22");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

}
