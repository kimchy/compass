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

package org.compass.core.test.transaction.type.jta;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.impl.ExistingCompassSession;
import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;

/**
 * @author kimchy
 */
public abstract class AbstractJTATests extends TestCase {

    private Compass compass;

    private Jotm jotm;

    protected abstract String getCompassConfig();

    protected void setUp() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (Exception e) {

        }

        jotm = new Jotm(true, true);
        Context ctx = new InitialContext();
        ctx.rebind("java:comp/UserTransaction", jotm.getUserTransaction());

        CompassConfiguration conf = new CompassConfiguration()
                .configure(getCompassConfig());
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    protected void tearDown() throws Exception {
        compass.close();
        compass.getSearchEngineIndexManager().deleteIndex();
        jotm.stop();
    }

    public void testJtaWithLocalTransaction() throws Exception {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginLocalTransaction();

        A a = new A();
        a.setId(1l);
        session.save(a);

        tr.commit();
        session.close();

        session = compass.openSession();
        tr = session.beginLocalTransaction();

        a = session.load(A.class, 1);
        assertNotNull(a);

        tr.commit();
        session.close();
    }

    public void testInnerUTManagement() throws Exception {
        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();

        // save a new instance of A
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);

        a = (A) session.get(A.class, id);
        assertNotNull(a);

        // check that if we open a new transaction within the current one it
        // will still work
        CompassSession newSession = compass.openSession();
        assertTrue(newSession instanceof ExistingCompassSession);
        assertTrue(session == ((ExistingCompassSession) newSession).getActualSession());
        CompassTransaction newTr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        // this one should not commit the jta transaction since the out
        // controlls it
        newTr.commit();
        newSession.close();

        tr.commit();

        // verify that the instance was saved
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);

        tr.commit();
        session.close();
    }

    public void testOuterUTManagementWithCommit() throws Exception {
        Context ctx = new InitialContext();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();

        CompassSession oldSession = session;
        session = compass.openSession();
        assertTrue(session instanceof ExistingCompassSession);
        assertTrue(oldSession == ((ExistingCompassSession) session).getActualSession());
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();

        ut.commit();

        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();
    }

    public void testOuterUTManagementWithCommitAndNoSessionOrTransactionManagement() throws Exception {
        Context ctx = new InitialContext();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();

        CompassSession session = compass.openSession();
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);
        a = (A) session.get(A.class, id);
        assertNotNull(a);

        CompassSession oldSession = session;
        session = compass.openSession();
        assertTrue(session instanceof ExistingCompassSession);
        assertTrue(oldSession == ((ExistingCompassSession) session).getActualSession());
        a = (A) session.get(A.class, id);
        assertNotNull(a);

        ut.commit();

        // now check that things were committed
        // here we do need explicit session/transaciton mangement
        // just cause we are lazy and want to let Comapss to manage JTA
        session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();
    }

    public void testOuterUTManagementWithRollback() throws Exception {
        Context ctx = new InitialContext();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();

        ut.rollback();

        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNull(a);
        tr.commit();
        session.close();
    }

    public void testOuterUTManagementWithSuspend() throws Exception {
        Context ctx = new InitialContext();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();

        CompassSession session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);
        a = (A) session.get(A.class, id);
        assertNotNull(a);

        TransactionManager transactionManager = Current.getTransactionManager();
        Transaction jtaTrans = transactionManager.suspend();

        UserTransaction newUt = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        newUt.begin();
        CompassSession newSession = compass.openSession();
        assertTrue(session != newSession);
        assertFalse(newSession instanceof ExistingCompassSession);
        CompassTransaction newTr = newSession.beginTransaction();
        a = (A) newSession.get(A.class, id);
        assertNull(a);
        newTr.commit();
        newSession.close();
        newUt.commit();

        transactionManager.resume(jtaTrans);

        tr.commit();
        ut.commit();

        session = compass.openSession();
        tr = session.beginTransaction();
        a = (A) session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();
    }

    public void testOuterUTManagementWithSuspendAndNoSessionOrTransactionManagement() throws Exception {
        Context ctx = new InitialContext();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();

        CompassSession session = compass.openSession();
        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        session.save(a);
        a = (A) session.get(A.class, id);
        assertNotNull(a);

        TransactionManager transactionManager = Current.getTransactionManager();
        Transaction jtaTrans = transactionManager.suspend();

        UserTransaction newUt = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        newUt.begin();
        CompassSession newSession = compass.openSession();
        assertFalse(newSession instanceof ExistingCompassSession);
        assertTrue(session != newSession);
        a = (A) newSession.get(A.class, id);
        assertNull(a);
        newUt.commit();

        transactionManager.resume(jtaTrans);

        ut.commit();

        // here we are lazy and let Compass manage a verifying JTa transaction
        session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        a = session.get(A.class, id);
        assertNotNull(a);
        tr.commit();
        session.close();
    }

}
