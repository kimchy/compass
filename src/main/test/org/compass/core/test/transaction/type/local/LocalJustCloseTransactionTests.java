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

package org.compass.core.test.transaction.type.local;

import org.compass.core.CompassSession;
import org.compass.core.impl.ExistingCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * Local transaction tests that just does session#close (and not session#commit()).
 *
 * @author kimchy
 */
public class LocalJustCloseTransactionTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"transaction/type/local/A.cpm.xml"};
    }

    public void testNestedLocalTransactionsWithCommits() throws Exception {
        CompassSession session = openSession();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);

        CompassSession nestedSession = openSession();
        assertTrue(nestedSession instanceof ExistingCompassSession);
        assertTrue(session == ((ExistingCompassSession) nestedSession).getActualSession());
        a = nestedSession.get(A.class, id);
        assertNotNull(a);
        nestedSession.close();

        a = session.get(A.class, id);
        assertNotNull(a);

        session.close();

        session = openSession();

        a = session.get(A.class, id);
        assertNotNull(a);

        session.close();
    }

    public void testNestedLocalTransactionsWithNestedRollback() throws Exception {
        CompassSession session = openSession();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);

        CompassSession nestedSession = openSession();
        assertTrue(nestedSession instanceof ExistingCompassSession);
        assertTrue(session == ((ExistingCompassSession) nestedSession).getActualSession());
        a = (A) nestedSession.get(A.class, id);
        assertNotNull(a);
        nestedSession.rollback();

        try {
            a = session.get(A.class, id);
            fail();
        } catch (Exception e) {
            // do nothing
        }
        session.close();
    }

    public void testNestedLocalTransacitonWithCommitsAndNoNestedBegin() throws Exception {
        CompassSession session = openSession();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);

        CompassSession nestedSession = openSession();
        assertTrue(nestedSession instanceof ExistingCompassSession);
        assertTrue(session == ((ExistingCompassSession) nestedSession).getActualSession());
        a = nestedSession.get(A.class, id);
        assertNotNull(a);
        // this actually might not be called as well
        // nestedSession.close();

        a = session.get(A.class, id);
        assertNotNull(a);

        session.close();

        // verify that the instance was saved
        session = openSession();
        a = session.get(A.class, id);
        assertNotNull(a);

        session.close();
    }
}