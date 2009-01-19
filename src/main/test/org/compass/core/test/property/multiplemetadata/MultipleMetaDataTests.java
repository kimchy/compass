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

package org.compass.core.test.property.multiplemetadata;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MultipleMetaDataTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/multiplemetadata/mapping.cpm.xml"};
    }

    public void testMultipleMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id1 = new Long(1);
        MultipleMetaData o1 = new MultipleMetaData();
        o1.setId(id1);
        o1.setValue1("test1");
        o1.setValue2("test2");
        session.save(o1);
        Long id2 = new Long(2);
        MultipleMetaData o2 = new MultipleMetaData();
        o2.setId(id2);
        o2.setValue1("test1");
        o2.setValue2("testNO");
        session.save(o2);

        CompassHits list = session.find("join2:test1");
        assertEquals(2, list.getLength());

        list = session.find("value21:testNO");
        assertEquals(1, list.getLength());

        tr.commit();
        session.close();
    }
}
