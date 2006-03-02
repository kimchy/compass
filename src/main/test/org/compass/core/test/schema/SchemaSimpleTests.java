package org.compass.core.test.schema;

import junit.framework.TestCase;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class SchemaSimpleTests extends TestCase {

    public void testSimpleSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/simple.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals(settings.getSetting(CompassEnvironment.NAME), "default");
        assertEquals(settings.getSetting(CompassEnvironment.CONNECTION), "file://target/test-index");
    }
}
