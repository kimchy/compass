package org.compass.annotations.test.converter;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.annotations.test.Converted;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.CompassHits;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ConverterTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addPackage("org.compass.annotations.test.converter");
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Converted("id1", "id2");
        a.value = new Converted("value1", "value2");

        session.save(a);

        a = (A) session.load(A.class, a.id);
        assertEquals("id1", a.id.value1);
        assertEquals("id2", a.id.value2);
        assertEquals("value1", a.value.value1);
        assertEquals("value2", a.value.value2);

        Resource resource = session.loadResource(A.class, a.id);
        assertEquals("id1#id2", resource.get("$/A/id"));
        assertEquals("value1#value2", resource.get("value"));

        CompassHits hits = session.find("value1#value2");
        assertEquals(1, hits.length());


        tr.commit();
        session.close();
    }
}
