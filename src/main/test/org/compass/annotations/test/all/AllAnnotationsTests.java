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

package org.compass.annotations.test.all;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class AllAnnotationsTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A1.class).addClass(A2.class).addClass(A3.class).addClass(A4.class);
    }

    public void testDisableAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A1 a = new A1();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertEquals(1, session.find("value:test").length());
        assertEquals(0, session.find("test").length());

        tr.commit();
        session.close();
    }

    public void testExcludeAliasTrue() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A2 a = new A2();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertEquals(0, session.find("a2").length());

        tr.commit();
        session.close();
    }

    public void testExcludeAliasFalse() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A3 a = new A3();
        a.id = 1;
        a.value = "test";
        session.save(a);

        assertEquals(1, session.find("a3").length());

        tr.commit();
        session.close();
    }

    public void testExcludePropertyFromAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A4 a = new A4();
        a.id = 1;
        a.value1 = "test";
        a.value2 = "best";
        session.save(a);

        assertEquals(1, session.find("test").length());
        assertEquals(0, session.find("best").length());

        tr.commit();
        session.close();
    }
}
