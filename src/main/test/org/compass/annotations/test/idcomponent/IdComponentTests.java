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

package org.compass.annotations.test.idcomponent;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class IdComponentTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSimpleIdCompoent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.b = new B(1, 2);
        a.b.value = "test1";
        a.value = "value1";

        session.save(a);

        a = new A();
        a.b = new B(1, 2);
        a = session.load(A.class, a);
        assertNotNull(a);
        assertNotNull(a.b);
        a = session.load(A.class, a.b);
        assertNotNull(a);
        assertNotNull(a.b);

        assertEquals(1, session.find("value1").length());
        assertEquals(1, session.find("test1").length());

        tr.commit();
        session.close();
    }
}
