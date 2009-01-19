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

package org.compass.core.test.rebuildcompass;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class RebuildCompassTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"rebuildcompass/A.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setLongSetting(CompassEnvironment.Rebuild.SLEEP_BEFORE_CLOSE, 100);
    }

    public void testRebuildCompass() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();
        A a = new A();
        a.id = 1;
        a.value = "avalue";
        session.save("a", a);
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        assertNotNull(session.get("a", 1));
        tr.commit();
        session.close();

        getCompass().getConfig().addClass(B.class);
        getCompass().rebuild();

        session = openSession();
        tr = session.beginTransaction();
        B b = new B();
        b.id = 1;
        b.value = "bvalue";
        session.save("b", b);
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        assertNotNull(session.get("a", 1));
        assertNotNull(session.get("b", 1));
        tr.commit();
        session.close();

        getCompass().getConfig().removeMappingByClass(A.class);
        getCompass().rebuild();

        session = openSession();
        tr = session.beginTransaction();
        assertNull(session.get("a", 1));
        assertNotNull(session.get("b", 1));
        tr.commit();
        session.close();

        Thread.sleep(200);
    }
}
