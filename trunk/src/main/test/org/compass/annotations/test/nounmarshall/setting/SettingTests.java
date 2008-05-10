package org.compass.annotations.test.nounmarshall.setting;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class SettingTests extends AbstractAnnotationsTestCase {

    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, false);
    }


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testNoUnmarshall() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.value2 = "value2";
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);
        assertEquals("A#1#", resource.getUID());

        a = session.load(A.class, 1);
        assertEquals(1, a.id.longValue());
        assertNull(a.value);
        assertNull(a.value2);

        tr.commit();
        session.close();
    }

}
