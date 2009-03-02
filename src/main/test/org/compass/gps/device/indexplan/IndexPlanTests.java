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

package org.compass.gps.device.indexplan;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.gps.impl.SingleCompassGps;

/**
 * @author kimchy
 */
public class IndexPlanTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class).addClass(C.class).addClass(D.class);
    }

    private Compass compass;

    private IndexPlanGpsDevice device;

    private SingleCompassGps gps;

    protected void setUp() throws Exception {
        // close the original Compass
        getCompass().close();

        super.setUp();
        compass  = buildCompass();
        getFileHandlerMonitor().verifyNoHandlers();
        device = new IndexPlanGpsDevice();
        device.setName("mock");
        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(device);
        gps.start();
    }

    protected void tearDown() throws Exception {
        gps.stop();
        compass.close();
        getFileHandlerMonitor().verifyNoHandlers();
        super.tearDown();
    }

    public void testSetupData() {
        setUpData(true);

        assertExists(A.class, 1);
        assertExists(A.class, 2);
        assertNotExists(A.class, 3);
        assertExists(B.class, 1);
        assertExists(B.class, 2);
        assertNotExists(B.class, 3);
        assertExists(C.class, 1);
        assertExists(C.class, 2);
        assertNotExists(C.class, 3);
        assertExists(D.class, 1);
        assertExists(D.class, 2);
        assertNotExists(D.class, 3);
    }

    public void testSimpleIndex() {
        setUpData(true);

        A a = new A();
        a.id = 3;
        a.value = "valuea3";
        device.add("a", a);

        gps.index();
        assertExists(A.class, 3);
        assertExists(A.class, 1);
        assertExists(A.class, 2);
        assertExists(B.class, 1);
        assertExists(B.class, 2);
        assertExists(C.class, 1);
        assertExists(C.class, 2);
        assertExists(D.class, 1);
        assertExists(D.class, 2);
    }

    public void testIndexJustA() {
        setUpData(true);

        // even if we clear everything, only a will be indexed, so b,c,d will still exist
        device.clear();

        A a = new A();
        a.id = 3;
        a.value = "valuea3";
        device.add("a", a);

        gps.index(A.class);
        assertExists(A.class, 3);
        assertNotExists(A.class, 1);
        assertNotExists(A.class, 2);
        assertExists(B.class, 1);
        assertExists(B.class, 2);
        assertExists(C.class, 1);
        assertExists(C.class, 2);
        assertExists(D.class, 1);
        assertExists(D.class, 2);
    }

    /**
     * Indexing just B will cause C to be included in the indexing as well because they share the
     * same sub index.
     */
    public void testIndexJustB() {
        setUpData(false);

        device.clear("a");
        device.clear("d");

        B b = new B();
        b.id = 3;
        b.value = "valueb3";
        device.add("b", b);

        gps.index(B.class);
        assertNotExists(A.class, 3);
        assertNotExists(A.class, 1);
        assertNotExists(A.class, 2);
        assertExists(B.class, 1);
        assertExists(B.class, 2);
        assertExists(B.class, 3);
        assertExists(C.class, 1);
        assertExists(C.class, 2);
        assertNotExists(D.class, 1);
        assertNotExists(D.class, 2);
    }

    public void testIndexJustD() {
        setUpData(false);

        device.clear("a");
        device.clear("b");

        D d = new D();
        d.id = 3;
        d.value = "valued3";
        device.add("d", d);

        gps.index(D.class);
        assertNotExists(A.class, 3);
        assertNotExists(A.class, 1);
        assertNotExists(A.class, 2);
        assertNotExists(B.class, 1);
        assertNotExists(B.class, 2);
        assertNotExists(B.class, 3);
        assertNotExists(C.class, 1);
        assertNotExists(C.class, 2);
        assertExists(D.class, 1);
        assertExists(D.class, 2);
        assertExists(D.class, 3);
    }

    private <T> void assertExists(Class<T> type, int id) {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        assertNotNull(session.get(type, id));
        tr.commit();
        session.close();
    }

    private <T> void assertNotExists(Class<T> type, int id) {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        assertNull(session.get(type, id));
        tr.commit();
        session.close();
    }

    private void setUpData(boolean index) {
        A a = new A();
        a.id = 1;
        a.value = "valuea1";
        device.add("a", a);

        a = new A();
        a.id = 2;
        a.value = "valuea2";
        device.add("a", a);

        B b = new B();
        b.id = 1;
        b.value = "bvalue1";
        device.add("b", b);

        b = new B();
        b.id = 2;
        b.value = "bvalue2";
        device.add("b", b);

        C c = new C();
        c.id = 1;
        c.value = "cvalue1";
        device.add("c", c);

        c = new C();
        c.id = 2;
        c.value = "cvalue2";
        device.add("c", c);

        D d = new D();
        d.id = 1;
        d.value = "dvalue1";
        device.add("d", d);

        d = new D();
        d.id = 2;
        d.value = "dvalue2";
        device.add("d", d);

        if (index) {
            gps.index();
        }
    }
}
