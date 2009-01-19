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

package org.compass.core.test.constant;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ConstantTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "constant/Constant.cpm.xml" };
    }

    public void testConstants() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A o = new A();
        o.setId(id);

        session.save(o);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        Resource r = session.loadResource("a", id);
        Property[] ps = r.getProperties("mvalue");
        assertEquals(2, ps.length);
        assertEquals("mValue11", ps[0].getStringValue());
        assertEquals("mValue12", ps[1].getStringValue());
        ps = r.getProperties("mvalue2");
        assertEquals(0, ps.length);

        tr.commit();
    }
}
