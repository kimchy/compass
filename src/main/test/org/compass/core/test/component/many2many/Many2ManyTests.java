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

package org.compass.core.test.component.many2many;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class Many2ManyTests extends AbstractTestCase {


    protected String[] getMappings() {
        return new String[]{"component/many2many/ManyToMany.cpm.xml"};
    }

    public void testManyToMany() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        ManyToMany1 many11 = new ManyToMany1();
        many11.id = new Long(1);
        many11.value = "many11";

        ManyToMany1 many12 = new ManyToMany1();
        many12.id = new Long(2);
        many12.value = "many12";

        ManyToMany2 many21 = new ManyToMany2();
        many21.id = new Long(1);
        many21.value = "many21";

        many11.many2.add(many21);
        many12.many2.add(many21);

        many21.many1.add(many11);
        many21.many1.add(many12);

        session.save(many11);
        session.save(many12);
        session.save(many21);

        many21 = (ManyToMany2) session.load("many2", new Long(1));
        assertEquals("many21", many21.value);
        assertEquals(2, many21.many1.size());
        assertEquals("many11", ((ManyToMany1) many21.many1.get(0)).value);
        assertEquals("many12", ((ManyToMany1) many21.many1.get(1)).value);

        many11 = (ManyToMany1) session.load("many1", new Long(1));
        assertEquals("many11", many11.value);
        assertEquals(1, many11.many2.size());
        assertEquals("many21", ((ManyToMany2) many11.many2.get(0)).value);

        tr.commit();
        session.close();
    }

}
