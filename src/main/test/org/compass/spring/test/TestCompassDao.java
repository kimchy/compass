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

import org.compass.spring.CompassDaoSupport;

public class TestCompassDao extends CompassDaoSupport {

    public void saveA1() {
        A a = new A();
        a.setId(new Long(1));
        a.setValue1("a1 value1");
        a.setValue2("a1 value2");
        getCompassTemplate().save(a);
    }

    public void saveA2() {
        A a = new A();
        a.setId(new Long(2));
        a.setValue1("a2 value1");
        a.setValue2("a2 value2");
        getCompassTemplate().save(a);
    }

    public A getA1() {
        return (A) getCompassTemplate().get(A.class, new Long(1));
    }

    public A getA2() {
        return (A) getCompassTemplate().get(A.class, new Long(2));
    }
}
