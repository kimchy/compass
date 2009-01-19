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

package org.compass.core.test.component.comp1;

import java.util.ArrayList;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class Comp1Tests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/comp1/mapping.cpm.xml"};
    }

    public void testWithSpecialPerson() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Person person = new SpecialPerson();
        person.id = 1;
        person.description = "test person";
        person.names = new ArrayList();
        PersonName personName = new PersonName();
        personName.names = new ArrayList();
        personName.names.add("name1");
        personName.names.add("name2");
        person.names.add(personName);
        session.save(person);

        User user = new User();
        user.id = 1;
        user.description = "test user";
        user.identity = person;
        session.save(user);
        
        person = (Person) session.load(SpecialPerson.class, new Integer(1));
        assertEquals("test person", person.description);
        assertEquals(1, person.names.size());
        personName = (PersonName) person.names.get(0);
        assertEquals(2, personName.names.size());

        user = (User) session.load(User.class, "1");
        assertEquals("test user", user.description);
        person = user.identity;
        assertEquals("test person", person.description);
        assertEquals(1, person.names.size());
        personName = (PersonName) person.names.get(0);
        assertEquals(2, personName.names.size());

        tr.commit();
        session.close();
    }

    public void testWithPerson() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Person person = new Person();
        person.id = 1;
        person.description = "test person";
        person.names = new ArrayList();
        PersonName personName = new PersonName();
        personName.names = new ArrayList();
        personName.names.add("name1");
        personName.names.add("name2");
        person.names.add(personName);
        session.save(person);

        User user = new User();
        user.id = 1;
        user.description = "test user";
        user.identity = person;
        session.save(user);

        person = (Person) session.load(Person.class, new Integer(1));
        assertEquals("test person", person.description);
        assertEquals(1, person.names.size());
        personName = (PersonName) person.names.get(0);
        assertEquals(2, personName.names.size());

        user = (User) session.load(User.class, "1");
        assertEquals("test user", user.description);
        person = user.identity;
        assertEquals("test person", person.description);
        assertEquals(1, person.names.size());
        personName = (PersonName) person.names.get(0);
        assertEquals(2, personName.names.size());

        tr.commit();
        session.close();
    }
}
