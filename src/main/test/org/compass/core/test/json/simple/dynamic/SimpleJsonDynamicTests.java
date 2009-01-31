package org.compass.core.test.json.simple.dynamic;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.json.RawAliasedJsonObject;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleJsonDynamicTests extends AbstractTestCase {

    @Override
    protected String[] getMappings() {
        return new String[]{"json/simple/dynamic/mapping.cpm.xml"};
    }

    public void testSimpleDynamicJson() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        RawAliasedJsonObject jsonObject = new RawAliasedJsonObject("a", "{id : 1, value : \"test\", value2 : \"test2\", value3 : 2}");
        session.save(jsonObject);

        Resource resource = session.loadResource("a", 1);
        assertEquals("test", resource.getValue("value"));
        assertEquals("test2", resource.getValue("value2"));
        assertEquals("2", resource.getValue("value3"));

        assertTrue(resource.getProperty("value").isTokenized());
        assertFalse(resource.getProperty("value3").isTokenized());

        tr.commit();
        session.close();
    }
}