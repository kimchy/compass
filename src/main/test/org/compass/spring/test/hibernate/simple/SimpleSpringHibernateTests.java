/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.spring.test.hibernate.simple;

import junit.framework.Assert;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGps;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SimpleSpringHibernateTests extends AbstractDependencyInjectionSpringContextTests {

    protected SessionFactory sessionFactory;

    protected Compass compass;

    protected CompassGps compassGps;

    public SimpleSpringHibernateTests() {
        super();
        setPopulateProtectedVariables(true);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"org/compass/spring/test/hibernate/simple/context.xml"};
    }

    public void testSpringHibernateTransaction() throws Exception {
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();


        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Simple simple = new Simple();
        simple.setId(1);
        simple.setValue("value1");
        session.save("simple", simple);
        simple = new Simple();
        simple.setId(2);
        simple.setValue("value2");
        session.save("simple", simple);
        simple = new Simple();
        simple.setId(3);
        simple.setValue("value3");
        session.save("simple", simple);

        SimpleBase simpleBase = new SimpleBase();
        simpleBase.setId(1);
        simpleBase.setValue("value");
        session.save(simpleBase);

        SimpleExtend simpleExtend = new SimpleExtend();
        simpleExtend.setId(2);
        simpleExtend.setValue("value");
        simpleExtend.setValueExtend("valueExtended");
        session.save(simpleExtend);
        transaction.commit();
        session.close();

        compassGps.index();

        CompassSession sess = compass.openSession();
        simple = sess.load(Simple.class, 1);
        Assert.assertEquals("value1", simple.getValue());
        simple = sess.load(Simple.class, 2);
        Assert.assertEquals("value2", simple.getValue());

        CompassHits hits = sess.find("value1");
        Assert.assertEquals(1, hits.length());
        sess.close();
    }
}
