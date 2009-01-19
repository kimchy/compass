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

package org.compass.spring.test.context;

import junit.framework.TestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.spi.InternalCompass;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author kimchy
 */
public class SpringCompassContextTests extends TestCase {

    public void testSimpleDaoWithCompassContext() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("org/compass/spring/test/context/simple-context.xml");

        CompassContextDao dao = (CompassContextDao) ctx.getBean("dao");
        assertEquals("compass", ((InternalCompass)dao.compass).getName());

        // using local transaciton, check that outer/inner works
        CompassSession session = dao.session;
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        dao.session.save(a);

        dao.session.load(A.class, 1);

        tr.commit();
        session.close();

        ((DisposableBean) ctx).destroy();
    }

    public void testSimpleDaoWithLocalCompassSession() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("org/compass/spring/test/context/simple-context.xml");

        CompassContextDao2 dao = (CompassContextDao2) ctx.getBean("dao2");
        assertEquals("compass", ((InternalCompass)dao.compass).getName());

        // using local transaciton, check that outer/inner works
        CompassSession session = dao.session;
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        dao.session.save(a);

        dao.session.load(A.class, 1);

        tr.commit();
        session.close();

        ((DisposableBean) ctx).destroy();
    }
}
