package org.compass.core.test.json.indexname;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonIndexNameTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"json/indexname/mapping.cpm.xml"};
    }

    public void testSimpleJsonArray() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", arr : [1, 2]}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("ivalue"));
        assertEquals(2, resource.getProperties("iarr").length);

        assertEquals(1, session.find("a.arr:1").length());

        tr.commit();
        session.close();
    }
}