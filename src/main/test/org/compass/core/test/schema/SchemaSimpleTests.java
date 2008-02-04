package org.compass.core.test.schema;

import java.util.Map;

import junit.framework.TestCase;
import org.compass.core.accessor.DirectPropertyAccessor;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.naming.DynamicPropertyNamingStrategy;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.optimizer.NullOptimizer;
import org.compass.core.lucene.engine.store.jdbc.C3P0DataSourceProvider;
import org.compass.core.lucene.engine.store.jdbc.DbcpDataSourceProvider;
import org.compass.core.lucene.engine.store.jdbc.DriverManagerDataSourceProvider;
import org.compass.core.lucene.engine.store.jdbc.ExternalDataSourceProvider;
import org.compass.core.lucene.engine.store.jdbc.JndiDataSourceProvider;
import org.compass.core.transaction.JTASyncTransactionFactory;
import org.compass.core.transaction.manager.JBoss;

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

    public void testLocalCacheSettings() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/localcache.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("ram://", settings.getSetting(LuceneEnvironment.LocalCache.PREFIX + "." + LuceneEnvironment.LocalCache.DEFAULT_NAME + "." + LuceneEnvironment.LocalCache.CONNECTION));
        assertEquals("file://", settings.getSetting(LuceneEnvironment.LocalCache.PREFIX + ".a." + LuceneEnvironment.LocalCache.CONNECTION));
    }

    public void testIndexDeletionPolicyKeepLastCommit() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/idp-keeplastcommit.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(LuceneEnvironment.IndexDeletionPolicy.KeepLastCommit.NAME, settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE));
    }

    public void testIndexDeletionPolicyKeepAll() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/idp-keepall.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(LuceneEnvironment.IndexDeletionPolicy.KeepAll.NAME, settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE));
    }

    public void testIndexDeletionPolicyKeepNoneOnInit() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/idp-keepnoneoninit.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(LuceneEnvironment.IndexDeletionPolicy.KeepNoneOnInit.NAME, settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE));
    }

    public void testIndexDeletionPolicyExpirationTime() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/idp-expirationtime.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.NAME, settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE));
        assertEquals("10", settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.EXPIRATION_TIME_IN_SECONDS));
    }

    public void testIndexDeletionPolicyKeepLastN() {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/idp-keeplastn.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(LuceneEnvironment.IndexDeletionPolicy.KeepLastN.NAME, settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE));
        assertEquals("10", settings.getSetting(LuceneEnvironment.IndexDeletionPolicy.KeepLastN.NUM_TO_KEEP));
    }

    public void testLockFactory() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/lockfactory.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("nativefs", settings.getSetting(LuceneEnvironment.LockFactory.TYPE));
        assertEquals("test/#subindex#", settings.getSetting(LuceneEnvironment.LockFactory.PATH));
    }

    public void testRamTransLog() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/ramtranslog.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("ram://", settings.getSetting(LuceneEnvironment.Transaction.ReadCommittedTransLog.CONNECTION));
        assertEquals("true", settings.getSetting(LuceneEnvironment.Transaction.ReadCommittedTransLog.OPTIMIZE_TRANS_LOG));
    }

    public void testFsTransLog() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/fstranslog.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("file://", settings.getSetting(LuceneEnvironment.Transaction.ReadCommittedTransLog.CONNECTION));
        assertEquals("false", settings.getSetting(LuceneEnvironment.Transaction.ReadCommittedTransLog.OPTIMIZE_TRANS_LOG));
    }

    public void testQueryParser() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/queryParser.cfg.xml");

        CompassSettings settings = conf.getSettings();

        Map groupSettings = settings.getSettingGroups(LuceneEnvironment.QueryParser.PREFIX);
        assertEquals(1, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("test");
        assertNotNull(settings);
        assertEquals("eg.QueryParser", settings.getSetting(LuceneEnvironment.QueryParser.TYPE));
        assertEquals("value1", settings.getSetting("param1"));
    }

    public void testOsem() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/osem.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("false", settings.getSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL));
        assertEquals("tokenized", settings.getSetting(CompassEnvironment.Osem.MANAGED_ID_INDEX));
    }

    public void testLocking() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/locking.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("15", settings.getSetting(LuceneEnvironment.Transaction.LOCK_TIMEOUT));
        assertEquals("200", settings.getSetting(LuceneEnvironment.Transaction.LOCK_POLL_INTERVAL));
    }

    public void testOptimizer() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/optimizer.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals(NullOptimizer.class.getName(), settings.getSetting(LuceneEnvironment.Optimizer.TYPE));
        assertEquals("true", settings.getSetting(LuceneEnvironment.Optimizer.SCHEDULE));
        assertEquals("90", settings.getSetting(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD));
    }

    public void testDirectoryWrapperProvider() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/wrapper-connection.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("eg.DWPImpl", settings.getSetting(LuceneEnvironment.DirectoryWrapper.PREFIX + ".test."
                + LuceneEnvironment.DirectoryWrapper.TYPE));
        assertEquals("value1", settings.getSetting(LuceneEnvironment.DirectoryWrapper.PREFIX + ".test.setting1"));
    }

    public void testRamConnectionSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/ram-connection.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("default", settings.getSetting(CompassEnvironment.NAME));
        assertEquals("ram://target/test-index", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testMmapConnectionSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/mmap-connection.cfg.xml");

        CompassSettings settings = conf.getSettings();

        assertEquals("default", settings.getSetting(CompassEnvironment.NAME));
        assertEquals("mmap://target/test-index", settings.getSetting(CompassEnvironment.CONNECTION));
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

    public void testPropertyNamingStrategySchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/propertyNamingStrategy.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(DynamicPropertyNamingStrategy.class.getName(), settings.getSetting(CompassEnvironment.NamingStrategy.TYPE));
    }

    public void testJtaSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jta.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals(JTASyncTransactionFactory.class.getName(), settings.getSetting(CompassEnvironment.Transaction.FACTORY));
        assertEquals("true", settings.getSetting(CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION));
        assertEquals(JBoss.class.getName(), settings.getSetting(CompassEnvironment.Transaction.MANAGER_LOOKUP));
    }

    public void testJdbcDriverManagerSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-drivermanager.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.MANAGED));
        assertEquals(DriverManagerDataSourceProvider.class.getName(),
                settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS));
        assertEquals("testusername", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME));
        assertEquals("testpassword", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD));
        assertEquals("testDriverClass", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS));
        assertEquals("jdbc://testurl", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testJdbcDbcp() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-dbcp.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.MANAGED));
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.DISABLE_SCHEMA_OPERATIONS));
        assertEquals(DbcpDataSourceProvider.class.getName(),
                settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS));
        assertEquals("testusername", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME));
        assertEquals("testpassword", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD));
        assertEquals("testDriverClass", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS));
        assertEquals("jdbc://testurl", settings.getSetting(CompassEnvironment.CONNECTION));
        assertEquals("10", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_ACTIVE));
        assertEquals("5", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_WAIT));
        assertEquals("2", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_IDLE));
        assertEquals("3", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.INITIAL_SIZE));
        assertEquals("4", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MIN_IDLE));
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.POOL_PREPARED_STATEMENTS));
        assertEquals("2", settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.DEFAULT_TRANSACTION_ISOLATION));
    }

    public void testJdbcC3p0() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-c3p0.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.MANAGED));
        assertEquals("true", settings.getSetting(LuceneEnvironment.JdbcStore.DISABLE_SCHEMA_OPERATIONS));
        assertEquals(C3P0DataSourceProvider.class.getName(),
                settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS));
        assertEquals("testusername", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME));
        assertEquals("testpassword", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD));
        assertEquals("testDriverClass", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS));
        assertEquals("jdbc://testurl", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testJdbcJndiSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-jndi.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("false", settings.getSetting(LuceneEnvironment.JdbcStore.MANAGED));
        assertEquals(JndiDataSourceProvider.class.getName(),
                settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS));
        assertEquals("testusername", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME));
        assertEquals("testpassword", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD));
        assertEquals("jdbc://testds", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testJdbcExternalSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-external.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("false", settings.getSetting(LuceneEnvironment.JdbcStore.MANAGED));
        assertEquals(ExternalDataSourceProvider.class.getName(),
                settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS));
        assertEquals("testusername", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME));
        assertEquals("testpassword", settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD));
        assertEquals("jdbc://", settings.getSetting(CompassEnvironment.CONNECTION));
    }

    public void testJdbcFESchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-fe.cfg.xml");

        CompassSettings settings = conf.getSettings();
        String defaultPrefix = LuceneEnvironment.JdbcStore.FileEntry.PREFIX + ".__default__.";
        assertEquals("4096", settings.getSetting(defaultPrefix + LuceneEnvironment.JdbcStore.FileEntry.INDEX_INPUT_BUFFER_SIZE));
        assertEquals("4096", settings.getSetting(defaultPrefix + LuceneEnvironment.JdbcStore.FileEntry.INDEX_OUTPUT_BUFFER_SIZE));
    }

    public void testJdbcDDLSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/jdbc-ddl.cfg.xml");

        CompassSettings settings = conf.getSettings();
        assertEquals("myname", settings.getSetting(LuceneEnvironment.JdbcStore.DDL.NAME_NAME));
        assertEquals("70", settings.getSetting(LuceneEnvironment.JdbcStore.DDL.NAME_LENGTH));
        assertEquals("mysize", settings.getSetting(LuceneEnvironment.JdbcStore.DDL.SIZE_NAME));
    }

    public void testConvertersSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/converters.cfg.xml");

        CompassSettings settings = conf.getSettings();
        Map groupSettings = settings.getSettingGroups(CompassEnvironment.Converter.PREFIX);
        assertEquals(2, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("date");
        assertNotNull(settings);
        assertEquals("yyyy-MM-dd", settings.getSetting("format"));
        settings = (CompassSettings) groupSettings.get("myConverter");
        assertNotNull(settings);
    }

    public void testPropertyAcessorsSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/property-accessors.cfg.xml");

        CompassSettings settings = conf.getSettings();
        Map groupSettings = settings.getSettingGroups(CompassEnvironment.PropertyAccessor.PREFIX);
        assertEquals(1, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("myType");
        assertNotNull(settings);
        assertEquals(DirectPropertyAccessor.class.getName(), settings.getSetting("type"));
        assertEquals("value", settings.getSetting("test"));
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

    public void testHighlightersSchema() throws Exception {
        CompassConfiguration conf = new CompassConfiguration()
                .configure("/org/compass/core/test/schema/highlighters.cfg.xml");

        CompassSettings settings = conf.getSettings();
        Map groupSettings = settings.getSettingGroups(LuceneEnvironment.Highlighter.PREFIX);
        assertEquals(1, groupSettings.size());
        settings = (CompassSettings) groupSettings.get("test1");
        assertNotNull(settings);
        assertEquals(LuceneEnvironment.Highlighter.Fragmenter.TYPE_SIMPLE,
                settings.getSetting(LuceneEnvironment.Highlighter.Fragmenter.TYPE));
        assertEquals(90, settings.getSettingAsInt(LuceneEnvironment.Highlighter.Fragmenter.SIMPLE_SIZE, 0));
        assertEquals(LuceneEnvironment.Highlighter.Encoder.HTML,
                settings.getSetting(LuceneEnvironment.Highlighter.Encoder.TYPE));
        assertEquals(LuceneEnvironment.Highlighter.Formatter.SIMPLE,
                settings.getSetting(LuceneEnvironment.Highlighter.Formatter.TYPE));
        assertEquals("<tag>", settings.getSetting(LuceneEnvironment.Highlighter.Formatter.SIMPLE_PRE_HIGHLIGHT));
        assertEquals("</tag>", settings.getSetting(LuceneEnvironment.Highlighter.Formatter.SIMPLE_POST_HIGHLIGHT));
    }
}
