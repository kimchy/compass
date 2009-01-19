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

package org.compass.spring.test.aop;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CompassAopTests extends TestCase {

    private ClassPathXmlApplicationContext applicationContext;
    
    private MockDaoOrService mockDaoOrService;
    
    private Compass compass;
    
    private CompassTemplate compassTemplate;

    protected void setUp() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext(
                "org/compass/spring/test/aop/applicationContext.xml");
        mockDaoOrService = (MockDaoOrService) applicationContext.getBean("mockDaoOrService");
        compass = (Compass) applicationContext.getBean("compass");
        compass.getSearchEngineIndexManager().cleanIndex();
        compassTemplate = new CompassTemplate(compass);
    }

    protected void tearDown() throws Exception {
        compass.getSearchEngineIndexManager().deleteIndex();
        applicationContext.close();
    }

    public void testAop() throws Exception {
        Long id = new Long(1);
        A a = (A) compassTemplate.get(A.class, id);
        assertNull(a);
        a = new A();
        a.setId(id);
        a.setValue1("test");
        mockDaoOrService.create(a);

        a = (A) compassTemplate.get(A.class, id);
        assertNotNull(a);
        assertEquals("test", a.getValue1());
        
        a.setValue1("NewTest");
        mockDaoOrService.save(a);
        a = (A) compassTemplate.get(A.class, id);
        assertNotNull(a);
        assertEquals("NewTest", a.getValue1());
        
        mockDaoOrService.delete(a);
        a = (A) compassTemplate.get(A.class, id);
        assertNull(a);
    }
}
