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

package org.compass.spring.test.transaction;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.spring.transaction.SpringSyncTransactionFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JotmFactoryBean;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author kimchy
 */
public class SpringSyncTransactionTests extends TestCase {

    private Compass compass;

    private JotmFactoryBean jotmFactoryBean;

    private JtaTransactionManager transactionManager;

    protected void setUp() throws Exception {

        jotmFactoryBean = new JotmFactoryBean();

        transactionManager = new JtaTransactionManager();
        transactionManager.setUserTransaction((UserTransaction) jotmFactoryBean.getObject());
        transactionManager.afterPropertiesSet();

        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/spring/test/transaction/compass.springsync.cfg.xml");
        SpringSyncTransactionFactory.setTransactionManager(transactionManager);
        compass = conf.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();
    }

    protected void tearDown() throws Exception {
        compass.close();
        compass.getSearchEngineIndexManager().deleteIndex();

        jotmFactoryBean.destroy();
    }

    public void testWithCommitNoSessionOrTransactionManagment() throws Exception {

        final Long id = new Long(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                A a = new A();
                a.setId(id);
                session.save(a);
                a = (A) session.get(A.class, id);
                assertNotNull(a);

                CompassSession oldSession = session;
                session = compass.openSession();
                assertTrue(oldSession == session);
                a = (A) session.get(A.class, id);
                assertNotNull(a);
            }
        });

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                A a = (A) session.get(A.class, id);
                assertNotNull(a);
            }
        });
    }


    public void testWithCommit() throws Exception {

        final Long id = new Long(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                CompassTransaction tr = session.beginTransaction();
                A a = new A();
                a.setId(id);
                session.save(a);
                a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();

                CompassSession oldSession = session;
                session = compass.openSession();
                assertTrue(oldSession == session);
                tr = session.beginTransaction();
                a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();
            }
        });

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                CompassTransaction tr = session.beginTransaction();
                A a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();
            }
        });
    }

    public void testWithRollback() throws Exception {

        final Long id = new Long(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                CompassTransaction tr = session.beginTransaction();
                A a = new A();
                a.setId(id);
                session.save(a);
                a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();

                CompassSession oldSession = session;
                session = compass.openSession();
                assertTrue(oldSession == session);
                tr = session.beginTransaction();
                a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();

                status.setRollbackOnly();
            }
        });

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompassSession session = compass.openSession();
                CompassTransaction tr = session.beginTransaction();
                A a = (A) session.get(A.class, id);
                assertNull(a);
                tr.commit();
                session.close();
            }
        });
    }

    public void testWithSuspend() throws Exception {

        final Long id = new Long(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final CompassSession session = compass.openSession();
                CompassTransaction tr = session.beginTransaction();
                A a = new A();
                a.setId(id);
                session.save(a);
                a = (A) session.get(A.class, id);
                assertNotNull(a);
                tr.commit();
                session.close();

                // start a new transaction
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        CompassSession innerSession = compass.openSession();
                        assertTrue(session != innerSession);
                        CompassTransaction tr = innerSession.beginTransaction();
                        A a = (A) innerSession.get(A.class, id);
                        assertNull(a);
                        tr.commit();
                        innerSession.close();
                    }
                });
            }
        });
    }
}
