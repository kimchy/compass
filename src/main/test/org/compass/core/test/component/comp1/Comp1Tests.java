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

    public void testB1WithC1AndC2AsNulls() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Person person = new Person();
        person.id = 1;
        person.description = "test";
        person.names = new ArrayList();
        PersonName personName = new PersonName();
        personName.names = new ArrayList();
        personName.names.add("name1");
        personName.names.add("name2");
        person.names.add(personName);
        session.save(person);

        person = (Person) session.load(Person.class, new Integer(1));
        assertEquals("test", person.description);
        assertEquals(1, person.names.size());

        tr.commit();
        session.close();
    }
}
