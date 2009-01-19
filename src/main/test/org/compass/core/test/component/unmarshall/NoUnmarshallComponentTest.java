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

package org.compass.core.test.component.unmarshall;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NoUnmarshallComponentTest extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/unmarshall/mapping.cpm.xml"};
    }

    public void test() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Master master = new Master();
        master.setMasterProperty("masterProp1");
        Slave slave = new Slave();
        slave.setId(1);
        slave.setMaster(master);
        slave.setName("slave1");
        session.save(slave);

        Slave slave1 = session.load(Slave.class, 1);
        assertNotNull(slave1);
        assertEquals(1, slave1.getId());
        assertEquals("slave1", slave1.getName());
        assertNull(slave1.getMaster());

        tr.commit();
        session.close();

    }

}
