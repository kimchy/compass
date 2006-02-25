/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.test.accessor;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class AccessorTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "accessor/Accessor.cpm.xml" };
    }

    public void testAccessor() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A o = new A();
        o.setId(id);
        o.setPropertyValue("propVal");
        o.packagePropertyValue = "packagePropVal";
        o.privatePropertyValue = "privatePropVal";
        o.protectedPropertyValue = "protectedPropVal";
        o.updateFieldValue("fieldVal");
        o.updateProtectedFieldValue("protectedFieldVal");
        o.updatePublicFieldValue("publicFieldVal");
        o.packageFieldValue = "packageFieldVal";
        session.save(o);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        o = (A) session.load(A.class, id);
        assertEquals("propVal", o.getPropertyValue());
        assertEquals("packagePropVal", o.packagePropertyValue);
        assertEquals("privatePropVal", o.privatePropertyValue);
        assertEquals("protectedPropVal", o.protectedPropertyValue);
        assertEquals("fieldVal", o.checkFieldValue());
        assertEquals("protectedFieldVal", o.checkProtectedFieldValue());
        assertEquals("publicFieldVal", o.checkPublicFieldValue());
        assertEquals("packageFieldVal", o.packageFieldValue);

        tr.commit();
        session.close();
    }

    public void testProtectedConstructor() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        B o = new B(id);
        session.save(o);
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();
        o = (B) session.load(B.class, id);
        tr.commit();
        session.close();
    }

    public void testNoSetter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        C o = new C(id);
        o.setValue("value");
        session.save(o);
        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        o = (C) session.load(C.class, id);
        Resource resource = session.loadResource(C.class, id);
        assertEquals("value special", resource.get("special"));

        tr.commit();
        session.close();
    }
}
