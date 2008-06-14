package org.compass.core.test.json.object;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonObjectTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/object/mapping.cpm.xml"};
    }

    public void testDotPath() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        assertEquals(1, session.find("a.value:test").length());
        assertEquals(1, session.find("a.obj.objValue1:4").length());
        assertEquals(1, session.find("a.obj.arr:1").length());
        assertEquals(1, session.find("a.obj.arr:2").length());

        tr.commit();
        session.close();
    }

    public void testSimpleJsonObject() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(new Integer(4), resource.getObject("objValue1"));
        assertEquals(2, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);
        assertEquals(new Integer(1), resource.getObjects("arr")[0]);
        assertEquals(new Integer(2), resource.getObjects("arr")[1]);

        tr.commit();
        session.close();
    }

    public void testSimpleJsonObjectDouble() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", obj : { objValue1 : \"4\", arr : [1, 2]}}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals(2, resource.getValues("objValue1").length);
        assertEquals("4", resource.getValue("objValue1"));
        assertEquals(new Integer(4), resource.getObject("objValue1"));
        assertEquals(4, resource.getProperties("arr").length);
        assertEquals("1", resource.getValues("arr")[0]);
        assertEquals("2", resource.getValues("arr")[1]);
        assertEquals(new Integer(1), resource.getObjects("arr")[0]);
        assertEquals(new Integer(2), resource.getObjects("arr")[1]);

        tr.commit();
        session.close();
    }
}