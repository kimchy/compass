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

package org.compass.core.test.events;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.events.FilterOperation;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class EventTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"events/mapping.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_CREATE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new String[]{MockEventListener.class.getName()});
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_DELETE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new String[]{MockEventListener.class.getName()});
        settings.setGroupSettings(CompassEnvironment.Event.PREFIX_PRE_SAVE, "1",
                new String[]{CompassEnvironment.Event.TYPE},
                new String[]{MockEventListener.class.getName()});
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockEventListener.events.clear();
        MockEventListener.filter = FilterOperation.NO;
    }

    public void testPreCreateEvent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.create(a);

        assertNotNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preCreate").size());

        tr.commit();
        session.close();
    }

    public void testPreCreateFilterOperation() {
        MockEventListener.filter = FilterOperation.YES;
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.create(a);

        assertNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preCreate").size());

        tr.commit();
        session.close();
    }

    public void testPreDeleteEvent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.create(a);
        assertNotNull(session.get("a", 1));

        session.delete(a);
        assertNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preDelete").size());

        tr.commit();
        session.close();
    }

    public void testPreDeleteEventFilterOperation() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.create(a);
        assertNotNull(session.get("a", 1));

        MockEventListener.filter = FilterOperation.YES;
        session.delete(a);
        assertNotNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preDelete").size());

        tr.commit();
        session.close();
    }

    public void testPreSaveEvent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertNotNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preSave").size());

        tr.commit();
        session.close();
    }

    public void testPreSaveFilterOperation() {
        MockEventListener.filter = FilterOperation.YES;
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertNull(session.get("a", 1));

        assertEquals(1, MockEventListener.events.get("preSave").size());

        tr.commit();
        session.close();
    }
}
