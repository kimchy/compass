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

package org.compass.core.test.multialias;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MultiAliasTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "multialias/multialias.cpm.xml" };
    }

    public void testMultiAlias() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("VALUE 1");
        session.save("a1", a);

        a.setValue("VALUE 2");
        session.save("a2", a);

        a = (A) session.load("a1", id);
        assertEquals("VALUE 1", a.getValue());

        a = (A) session.load("a2", id);
        assertEquals("VALUE 2", a.getValue());

        CompassHits hits = session.find("VALUE");
        assertEquals(2, hits.getLength());

        tr.commit();
    }
}
