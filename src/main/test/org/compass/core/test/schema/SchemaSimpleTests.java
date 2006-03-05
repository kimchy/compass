package org.compass.core.test.schema;

import java.util.Map;

import junit.framework.TestCase;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class SchemaSimpleTests extends TestCase {

    public void testSimpleSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/simple.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("default", settings.getSetting(CompassEnvironment.NAME));
        assertEquals("file://target/test-index", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testPropertiesSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/properties.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("alias1", settings.getSetting(CompassEnvironment.Alias.NAME));
        assertEquals("all1", settings.getSetting(CompassEnvironment.All.NAME));
        assertEquals("yes", settings.getSetting(CompassEnvironment.All.TERM_VECTOR));
        assertEquals("analyzer1", settings.getSetting(LuceneEnvironment.ALL_ANALYZER));
    }

    public void testAnalyzersSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/analyzers.cfg.xml");

        CompassSettings settings = conf.getSettings();
        Map groupSettings = settings.getSettingGroups(LuceneEnvironment.Analyzer.PREFIX);
        assertEquals(1, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("test");
        assertNotNull(settings);
        assertEquals("Snowball", settings.getSetting(LuceneEnvironment.Analyzer.TYPE));
        assertEquals("Lovins", settings.getSetting(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE));
        assertEquals("+test,", settings.getSetting(LuceneEnvironment.Analyzer.STOPWORDS));

        settings = conf.getSettings();
        groupSettings = settings.getSettingGroups(LuceneEnvironment.AnalyzerFilter.PREFIX);
        assertEquals(1, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("test");
        assertNotNull(settings);
        assertEquals("org.compass.test.Test", settings.getSetting(LuceneEnvironment.AnalyzerFilter.TYPE));
        assertEquals("setValue", settings.getSetting("setName"));
    }
}
