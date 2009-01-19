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

package org.compass.spring.test;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CompassDaoSupportTests extends TestCase {

    private TestCompassDao dao;

    private ClassPathXmlApplicationContext applicationContext;

    protected void setUp() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext(
                "org/compass/spring/test/applicationContext.xml");
        dao = (TestCompassDao) applicationContext.getBean("testDAO");
        dao.getCompass().getSearchEngineIndexManager().cleanIndex();
    }

    protected void tearDown() throws Exception {
        dao.getCompass().getSearchEngineIndexManager().deleteIndex();
        applicationContext.close();
    }

    public void testSimpleDaoSupport() throws Exception {
        assertNull(dao.getA1());
        assertNull(dao.getA2());
        dao.saveA1();
        assertNotNull(dao.getA1());
        assertNull(dao.getA2());
        dao.saveA2();
        assertNotNull(dao.getA1());
        assertNotNull(dao.getA2());
    }
}
