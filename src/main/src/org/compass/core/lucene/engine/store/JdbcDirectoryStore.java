/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.lucene.engine.store;

import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.JdbcStoreException;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.apache.lucene.store.jdbc.dialect.Dialect;
import org.apache.lucene.store.jdbc.dialect.DialectResolver;
import org.apache.lucene.store.jdbc.index.FetchPerTransactionJdbcIndexInput;
import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.engine.event.SearchEngineLifecycleEventListener;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.store.jdbc.DataSourceProvider;
import org.compass.core.lucene.engine.store.jdbc.DriverManagerDataSourceProvider;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class JdbcDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(JdbcDirectoryStore.class);

    public static final String PROTOCOL = "jdbc://";

    private JdbcDirectorySettings jdbcSettings;

    private DataSource dataSource;

    private DataSourceProvider dataSourceProvider;

    private Dialect dialect;

    private boolean managed;

    private boolean disableSchemaOperation;

    private Map<String, JdbcTable> cachedJdbcTables = new ConcurrentHashMap<String, JdbcTable>();

    /**
     * Jdbc store should not support concurrent operations on the index within a single session.
     */
    @Override
    public boolean supportsConcurrentOperations() {
        return false;
    }

    /**
     * Jdbc store should not support concurrent operations on the index within a single session.
     */
    @Override
    public boolean supportsConcurrentCommits() {
        return false;
    }

    public void configure(CompassSettings settings) throws CompassException {
        String connection = settings.getSetting(CompassEnvironment.CONNECTION);
        String url = connection.substring(PROTOCOL.length());

        dataSourceProvider = (DataSourceProvider) settings.getSettingAsInstance(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                DriverManagerDataSourceProvider.class.getName());
        dataSourceProvider.configure(url, settings);
        this.dataSource = dataSourceProvider.getDataSource();

        String dialectClassName = settings.getSetting(LuceneEnvironment.JdbcStore.DIALECT, null);
        if (dialectClassName == null) {
            try {
                dialect = new DialectResolver().getDialect(dataSource);
            } catch (JdbcStoreException e) {
                throw new ConfigurationException("Failed to auto detect dialect", e);
            }
        } else {
            try {
                dialect = (Dialect) ClassUtils.forName(dialectClassName, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to configure dialect [" + dialectClassName + "]");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Using dialect [" + dialect.getClass().getName() + "]");
        }


        managed = settings.getSettingAsBoolean(LuceneEnvironment.JdbcStore.MANAGED, false);
        if (log.isDebugEnabled()) {
            log.debug("Using managed [" + managed + "]");
        }
        if (!managed) {
            this.dataSource = new TransactionAwareDataSourceProxy(this.dataSource);
        }

        disableSchemaOperation = settings.getSettingAsBoolean(LuceneEnvironment.JdbcStore.DISABLE_SCHEMA_OPERATIONS, false);
        if (log.isDebugEnabled()) {
            log.debug("Using disable schema operations [" + disableSchemaOperation + "]");
        }

        jdbcSettings = new JdbcDirectorySettings();
        jdbcSettings.setNameColumnName(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.NAME_NAME, jdbcSettings.getNameColumnName()));
        jdbcSettings.setValueColumnName(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.VALUE_NAME, jdbcSettings.getValueColumnName()));
        jdbcSettings.setSizeColumnName(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.SIZE_NAME, jdbcSettings.getSizeColumnName()));
        jdbcSettings.setLastModifiedColumnName(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.LAST_MODIFIED_NAME, jdbcSettings.getLastModifiedColumnName()));
        jdbcSettings.setDeletedColumnName(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.DELETED_NAME, jdbcSettings.getDeletedColumnName()));

        jdbcSettings.setNameColumnLength(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DDL.NAME_LENGTH, jdbcSettings.getNameColumnLength()));
        jdbcSettings.setValueColumnLengthInK(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DDL.VALUE_LENGTH, jdbcSettings.getValueColumnLengthInK()));

        jdbcSettings.setTableCatalog(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.TABLE_CATALOG, jdbcSettings.getTableCatalog()));
        jdbcSettings.setTableSchema(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.TABLE_SCHEME, jdbcSettings.getTableSchema()));
        jdbcSettings.setTableType(settings.getSetting(LuceneEnvironment.JdbcStore.DDL.TABLE_TYPE, jdbcSettings.getTableType()));

        jdbcSettings.setDeleteMarkDeletedDelta(settings.getSettingAsLong(LuceneEnvironment.JdbcStore.DELETE_MARK_DELETED_DELTA, jdbcSettings.getDeleteMarkDeletedDelta()));
        if (log.isDebugEnabled()) {
            log.debug("Using delete mark deleted older than [" + jdbcSettings.getDeleteMarkDeletedDelta() + "ms]");
        }
        jdbcSettings.setQueryTimeout(settings.getSettingAsInt(LuceneEnvironment.Transaction.LOCK_TIMEOUT, jdbcSettings.getQueryTimeout()));
        if (log.isDebugEnabled()) {
            log.debug("Using query timeout (transaction lock timeout) [" + jdbcSettings.getQueryTimeout() + "ms]");
        }

        try {
            jdbcSettings.setLockClass(settings.getSettingAsClass(LuceneEnvironment.JdbcStore.LOCK_TYPE, jdbcSettings.getLockClass()));
        } catch (ClassNotFoundException e) {
            throw new CompassException("Failed to create jdbc lock class [" + settings.getSetting(LuceneEnvironment.JdbcStore.LOCK_TYPE) + "]");
        }
        if (log.isDebugEnabled()) {
            log.debug("Using lock strategy [" + jdbcSettings.getLockClass().getName() + "]");
        }

        if (dialect.supportTransactionalScopedBlobs() &&
                !"true".equalsIgnoreCase(settings.getSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, "false"))) {
            // Use FetchPerTransaction is dialect supports it
            jdbcSettings.getDefaultFileEntrySettings().setClassSetting(JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING,
                    FetchPerTransactionJdbcIndexInput.class);
            if (log.isDebugEnabled()) {
                log.debug("Using transactional blobs (dialect supports it)");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using non transactional blobs (dialect does not supports it)");
            }
        }

        Map fileEntries = settings.getSettingGroups(LuceneEnvironment.JdbcStore.FileEntry.PREFIX);
        for (Iterator it = fileEntries.keySet().iterator(); it.hasNext();) {
            String fileEntryName = (String) it.next();
            CompassSettings compassFeSettings = (CompassSettings) fileEntries.get(fileEntryName);
            if (log.isInfoEnabled()) {
                log.info("Configuring file entry [" + fileEntryName + "] with settings [" + compassFeSettings + "]");
            }
            JdbcFileEntrySettings jdbcFileEntrySettings = jdbcSettings.getFileEntrySettingsWithoutDefault(fileEntryName);
            if (jdbcFileEntrySettings == null) {
                jdbcFileEntrySettings = new JdbcFileEntrySettings();
            }
            // iterate over all the settings and copy them to the jdbc settings
            for (Iterator feIt = compassFeSettings.keySet().iterator(); feIt.hasNext();) {
                String feSetting = (String) feIt.next();
                jdbcFileEntrySettings.setSetting(feSetting, compassFeSettings.getSetting(feSetting));
            }
            jdbcSettings.registerFileEntrySettings(fileEntryName, jdbcFileEntrySettings);
        }
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        String totalPath = subContext + "_" + subIndex;
        JdbcTable jdbcTable = cachedJdbcTables.get(totalPath);
        if (jdbcTable == null) {
            jdbcTable = new JdbcTable(jdbcSettings, dialect, totalPath);
            cachedJdbcTables.put(totalPath, jdbcTable);
        }
        JdbcDirectory dir = new JdbcDirectory(dataSource, jdbcTable);
        if (!disableSchemaOperation) {
            try {
                Boolean exists = indexExists(dir);
                if (exists == null) {
                    try {
                        exists = IndexReader.indexExists(dir);
                    } catch (IOException e) {
                        exists = false;
                    }
                }
                if (!exists) {
                    dir.create();
                }
            } catch (IOException e) {
                throw new SearchEngineException("Failed to create dir [" + totalPath + "]", e);
            }
        }
        return dir;
    }

    public Boolean indexExists(Directory dir) throws SearchEngineException {
        try {
            // for databases that fail if there is no table (like postgres)
            if (dialect.supportsTableExists()) {
                boolean tableExists = ((JdbcDirectory) dir).tableExists();
                if (!tableExists) {
                    return Boolean.FALSE;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to check if index exists", e);
        } catch (UnsupportedOperationException e) {
            // do nothing, let the base class check for it
        }
        return null;
    }

    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        try {
            if (disableSchemaOperation) {
                ((JdbcDirectory) dir).deleteContent();
            } else {
                ((JdbcDirectory) dir).delete();
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete index [" + subIndex + "]", e);
        }
    }

    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        JdbcDirectory jdbcDirectory = (JdbcDirectory) dir;
        try {
            jdbcDirectory.deleteContent();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete content of [" + subIndex + "]", e);
        }
    }

    public void performScheduledTasks(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        try {
            ((JdbcDirectory) dir).deleteMarkDeleted();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete mark deleted with jdbc for [" + subIndex + "]", e);
        }
    }

    public CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir) throws SearchEngineException {
        try {
            ((JdbcDirectory) dir).deleteContent();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete index content");
        }
        return new CopyFromHolder();
    }

    public void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager) {
        if (managed) {
            eventManager.registerLifecycleListener(new ManagedEventListeners());
        } else {
            eventManager.registerLifecycleListener(new NoneManagedEventListeners());
        }
    }

    public void close() {
        this.dataSourceProvider.closeDataSource();
    }

    /**
     * The Jdbc store does require transactional context when executing async operations.
     */
    public boolean requiresAsyncTransactionalContext() {
        return true;
    }

    private class ManagedEventListeners implements SearchEngineLifecycleEventListener {

        public void beforeBeginTransaction() throws SearchEngineException {

        }

        public void afterBeginTransaction() throws SearchEngineException {

        }

        public void afterPrepare() throws SearchEngineException {

        }

        public void afterCommit(boolean onePhase) throws SearchEngineException {
            Connection conn;
            try {
                conn = DataSourceUtils.getConnection(dataSource);
            } catch (JdbcStoreException e) {
                throw new SearchEngineException("Failed to get connection", e);
            }
            FetchPerTransactionJdbcIndexInput.releaseBlobs(conn);
            DataSourceUtils.releaseConnection(conn);
        }

        public void afterRollback() throws SearchEngineException {
            Connection conn;
            try {
                conn = DataSourceUtils.getConnection(dataSource);
            } catch (JdbcStoreException e) {
                throw new SearchEngineException("Failed to get connection", e);
            }
            FetchPerTransactionJdbcIndexInput.releaseBlobs(conn);
            DataSourceUtils.releaseConnection(conn);
        }

        public void close() throws SearchEngineException {

        }
    }

    private class NoneManagedEventListeners implements SearchEngineLifecycleEventListener {

        private Connection connection;

        public void beforeBeginTransaction() throws SearchEngineException {
            try {
                connection = DataSourceUtils.getConnection(dataSource);
            } catch (JdbcStoreException e) {
                throw new SearchEngineException("Failed to open db connection", e);
            }
        }

        public void afterBeginTransaction() throws SearchEngineException {
        }

        public void afterPrepare() throws SearchEngineException {
        }

        public void afterCommit(boolean onePhase) throws SearchEngineException {
            try {
                DataSourceUtils.commitConnectionIfPossible(connection);
            } catch (JdbcStoreException e) {
                throw new SearchEngineException("Failed to commit database transcation", e);
            } finally {
                DataSourceUtils.releaseConnection(connection);
                this.connection = null;
            }
        }

        public void afterRollback() throws SearchEngineException {
            try {
                DataSourceUtils.rollbackConnectionIfPossible(connection);
            } catch (JdbcStoreException e) {
                throw new SearchEngineException("Failed to rollback database transcation", e);
            } finally {
                DataSourceUtils.releaseConnection(connection);
                this.connection = null;
            }
        }

        public void close() throws SearchEngineException {
        }
    }
}
