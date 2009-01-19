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

package org.compass.core.test.component.cyclic3;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleCyclicNoFilterDuplicatesTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/cyclic3/Cyclic.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.FILTER_DUPLICATES, false);
    }

    public void testCyclicWithParent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.id = new Long(1);
        cyclic1.value = "cyclic1";

        Cyclic2 cyclic2 = new Cyclic2();
        cyclic2.value = "cyclic2";

        cyclic1.cyclic2 = cyclic2;
        cyclic2.cyclic1 = cyclic1;

        session.save("cyclic1", cyclic1);

        cyclic1 = (Cyclic1) session.load("cyclic1", new Long(1));
        assertNotNull(cyclic1.cyclic2);
        assertEquals("cyclic2", cyclic1.cyclic2.value);
        cyclic2 = cyclic1.cyclic2;
        assertNotNull(cyclic2);
        assertEquals("cyclic1", cyclic2.cyclic1.value);

        tr.commit();
        session.close();
    }

    public void testCyclicNullWithParent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.id = id;
        cyclic1.value = "cyclic1";

        session.save("cyclic1", cyclic1);

        cyclic1 = (Cyclic1) session.load("cyclic1", id);
        assertNull(cyclic1.cyclic2);

        tr.commit();
        session.close();
    }

    public void testCyclicWithComponent() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Cyclic1 cyclic1 = new Cyclic1();
        cyclic1.id = id;
        cyclic1.value = "cyclic1";

        Cyclic2 cyclic2 = new Cyclic2();
        cyclic2.id = id;
        cyclic2.value = "cyclic2";

        cyclic1.cyclic2 = cyclic2;
        cyclic2.cyclic1 = cyclic1;

        session.save("cyclic1c", cyclic1);
        session.save("cyclic2c", cyclic2);

        cyclic1 = (Cyclic1) session.load("cyclic1c", id);
        assertNotNull(cyclic1.cyclic2);
        assertEquals("cyclic2", cyclic1.cyclic2.value);
        cyclic2 = cyclic1.cyclic2;
        assertNotNull(cyclic2);
        assertEquals("cyclic1", cyclic2.cyclic1.value);

        // check that compass creates the same object (when ids are invoved
        // when in cyclic reference
        assertEquals(System.identityHashCode(cyclic1), System.identityHashCode(cyclic1.cyclic2.cyclic1));

        cyclic2 = (Cyclic2) session.load("cyclic2c", id);
        assertNotNull(cyclic2.cyclic1);
        assertEquals("cyclic1", cyclic2.cyclic1.value);
        cyclic1 = cyclic2.cyclic1;
        assertNotNull(cyclic1);
        assertEquals("cyclic2", cyclic1.cyclic2.value);

        // check that we don't go into cyclic hell
        Resource resource = session.loadResource("cyclic1c", id);
        assertEquals(3, resource.getProperties("value").length);
        assertNotNull(resource.getProperty("$/cyclic1c/id"));
        assertNotNull(resource.getProperty("$/cyclic1c/value"));
        assertNotNull(resource.getProperty("$/cyclic1c/cyclic2/id"));
        assertNotNull(resource.getProperty("$/cyclic1c/cyclic2/value"));
        assertNotNull(resource.getProperty("$/cyclic1c/cyclic2/cyclic1/id"));
        assertNotNull(resource.getProperty("$/cyclic1c/cyclic2/cyclic1/value"));
        assertNull(resource.getProperty("$/cyclic1c/cyclic2/cyclic1/cyclic2/id"));
        assertNull(resource.getProperty("$/cyclic1c/cyclic2/cyclic1/cyclic2/cyclic1/id"));

        tr.commit();
        session.close();
    }

}