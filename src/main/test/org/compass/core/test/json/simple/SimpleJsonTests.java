package org.compass.core.test.json.simple;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.JsonObject;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/simple/mapping.cpm.xml"};
    }

    public void testSimpleJson() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\"}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));

        tr.commit();
        session.close();
    }

    public void testSimpleJsonWithContent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("b", "{id : 1, value : \"test\"}");
        session.save(jsonObject);

        Resource resource = session.loadResource("b", 1);
        assertEquals("test", resource.getValue("value"));

        JsonObject obj = (JsonObject) session.load("b", 1);
        assertNotNull(obj);
        assertEquals(2, obj.entries().size());

        // make another round, now without using RAW
        session.save("b", obj);
        obj = (JsonObject) session.load("b", 1);
        assertNotNull(obj);
        assertEquals(2, obj.entries().size());

        tr.commit();
        session.close();
    }
}
