package org.compass.core.test.json.array;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonArrayTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/array/mapping.cpm.xml"};
    }

    public void testDotPath() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", arr : [1, 2]}");
        session.save(jsonObject);

        assertEquals(1, session.find("a.value:test").length());
        assertEquals(1, session.find("a.arr:1").length());
        assertEquals(1, session.find("a.arr:2").length());    

        tr.commit();
        session.close();
    }

    public void testSimpleJsonArray() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", arr : [1, 2]}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals(2, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);
        assertEquals(new Integer(1), resource.getObjects("arr")[0]);
        assertEquals(new Integer(2), resource.getObjects("arr")[1]);

        tr.commit();
        session.close();
    }

    public void testSimpleJsonArrayWithIndexName() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("b", "{id : 1, value : \"test\", arr : [1, 2]}");
        session.save(jsonObject);

        Resource resource = session.loadResource("b", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals(2, resource.getProperties("xarr").length);
        assertEquals("1", resource.getValues("xarr")[0]);
        assertEquals("2", resource.getValues("xarr")[1]);

        tr.commit();
        session.close();
    }

    public void testSimpleJsonArrayWithObject() {
        CompassSession session = openSession();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("c", "{id : 1, value : \"test\", arr : [{ arr-value : \"test1\" }, { arr-value : \"test2\" }]}");
        session.save(jsonObject);

        Resource resource = session.loadResource("c", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals(2, resource.getValues("arr-value").length);
        assertEquals("test1", resource.getValues("arr-value")[0]);
        assertEquals("test2", resource.getValues("arr-value")[1]);

        session.commit();
    }
}