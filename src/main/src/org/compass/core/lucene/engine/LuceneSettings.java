/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.lucene.engine;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.compass.core.Property;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * A helper methods that holds most of the Lucene specific properties, initlizes
 * from {@link org.compass.core.config.CompassSettings}.
 *
 * @author kimchy
 */
public class LuceneSettings {

    private static final Log log = LogFactory.getLog(LuceneSettings.class);

    private String connection;

    private String defaultSearchPropery;

    private String allProperty;

    private Property.TermVector allPropertyTermVector;

    private String aliasProperty;

    private TransactionIsolation transactionIsolation;

    private Class transactionIsolationClass;

    private int maxMergeDocs;

    private int mergeFactor;

    private boolean useCompoundFile;

    private int maxFieldLength;

    private int maxBufferedDocs;

    private long transactionLockTimout;

    private long transactionCommitTimeout;

    private long cacheInvalidationInterval;

    private long indexManagerScheduleInterval;

    private String lockDir;

    public void configure(CompassSettings settings) throws SearchEngineException {
        connection = settings.getSetting(CompassEnvironment.CONNECTION);
        if (connection == null) {
            throw new SearchEngineException("Lucene connection must be set in the settings. Please set ["
                    + CompassEnvironment.CONNECTION + "]");
        }
        if (log.isDebugEnabled()) {
            log.debug("Using connection [" + connection + "]");
        }
        // the alias property
        aliasProperty = settings.getSetting(CompassEnvironment.ALIAS_PROPERTY,
                CompassEnvironment.DEFAULT_ALIAS_PROPERTY);
        if (log.isDebugEnabled()) {
            log.debug("Using alias property [" + aliasProperty + "]");
        }
        // get the all property
        allProperty = settings.getSetting(CompassEnvironment.ALL_PROPERTY, CompassEnvironment.DEFAULT_ALL_PROPERTY);
        if (log.isDebugEnabled()) {
            log.debug("Using default all property [" + allProperty + "]");
        }
        String allPropertyTermVectorSettings = settings.getSetting(CompassEnvironment.ALL_PROPERTY_TERM_VECTOR, "no");
        if (log.isDebugEnabled()) {
            log.debug("Using all property term vector [" + allPropertyTermVectorSettings + "]");
        }
        if ("no".equals(allPropertyTermVectorSettings)) {
            allPropertyTermVector = Property.TermVector.NO;
        } else if ("yes".equals(allPropertyTermVectorSettings)) {
            allPropertyTermVector = Property.TermVector.YES;
        } else if ("positions".equals(allPropertyTermVectorSettings)) {
            allPropertyTermVector = Property.TermVector.WITH_POSITIONS;
        } else if ("offsets".equals(allPropertyTermVectorSettings)) {
            allPropertyTermVector = Property.TermVector.WITH_OFFSETS;
        } else if ("positions_offsets".equals(allPropertyTermVectorSettings)) {
            allPropertyTermVector = Property.TermVector.WITH_POSITIONS_OFFSETS;
        } else {
            throw new SearchEngineException("Unrecognized term vector setting for the all property ["
                    + allPropertyTermVectorSettings + "]");
        }
        // get the default search term, defaults to the all property
        defaultSearchPropery = settings.getSetting(LuceneEnvironment.DEFAULT_SEARCH, allProperty);
        if (log.isDebugEnabled()) {
            log.debug("Using default search property [" + defaultSearchPropery + "]");
        }
        // build the trasnaction
        String transIsolationSetting = settings.getSetting(CompassEnvironment.Transaction.ISOLATION,
                CompassEnvironment.Transaction.ISOLATION_READ_COMMITTED);
        if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_NONE)) {
            transactionIsolation = TransactionIsolation.READ_COMMITTED;
        } else if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_READ_UNCOMMITTED)) {
            transactionIsolation = TransactionIsolation.READ_COMMITTED;
        } else if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_READ_COMMITTED)) {
            transactionIsolation = TransactionIsolation.READ_COMMITTED;
        } else if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_REPEATABLE_READ)) {
            transactionIsolation = TransactionIsolation.READ_COMMITTED;
        } else if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_SERIALIZABLE)) {
            transactionIsolation = TransactionIsolation.SERIALIZABLE;
        } else if (transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_BATCH_INSERT)) {
            transactionIsolation = TransactionIsolation.BATCH_INSERT;
        }
        String transIsolationClassSetting = settings.getSetting(CompassEnvironment.Transaction.ISOLATION_CLASS, null);
        if (transIsolationClassSetting != null) {
            try {
                transactionIsolationClass = ClassUtils.forName(transIsolationClassSetting);
            } catch (ClassNotFoundException e) {
                throw new SearchEngineException("Can't find transaction class [" + transIsolationClassSetting + "]", e);
            }
        }
        // lucene specifics parameters
        transactionLockTimout = settings.getSettingAsLong(LuceneEnvironment.Transaction.LOCK_TIMEOUT, 10) * 1000;
        if (log.isDebugEnabled()) {
            log.debug("Using transaction lock timeout [" + transactionLockTimout + "ms]");
        }
        transactionCommitTimeout = settings.getSettingAsLong(LuceneEnvironment.Transaction.COMMIT_TIMEOUT, 10) * 1000;
        if (log.isDebugEnabled()) {
            log.debug("Using transaction commit timeout [" + transactionCommitTimeout + "ms]");
        }
        Lock.LOCK_POLL_INTERVAL = settings.getSettingAsLong(LuceneEnvironment.Transaction.LOCK_POLL_INTERVAL, 100);
        if (log.isDebugEnabled()) {
            log.debug("Using lock poll interval [" + Lock.LOCK_POLL_INTERVAL + "ms]");
        }

        lockDir = settings.getSetting(LuceneEnvironment.Transaction.LOCK_DIR, System.getProperty("java.io.tmpdir"));
        File lockDirFile = new File(lockDir);
        if (!lockDirFile.exists()) {
            if (!lockDirFile.mkdirs()) {
                throw new SearchEngineException("Cannot create lock directory [" + lockDir + "]");
            }
        }
        System.setProperty("org.apache.lucene.lockDir", lockDir);
        if (log.isDebugEnabled()) {
            log.debug("Using lock directory [" + lockDir + "]");
        }

        maxMergeDocs = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_MERGE_DOCS, Integer.MAX_VALUE);
        useCompoundFile = settings.getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, true);
        if (log.isDebugEnabled()) {
            log.debug("Using compound format [" + useCompoundFile + "]");
        }

        // batch insert transaction settings
        mergeFactor = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, 10);
        maxBufferedDocs = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, 10);

        maxFieldLength = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_FIELD_LENGTH, 10000);

        // cach invalidation settings
        cacheInvalidationInterval = settings.getSettingAsLong(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, 5000);
        if (log.isDebugEnabled()) {
            log.debug("Using cach invalidation interval [" + cacheInvalidationInterval + "ms]");
        }
        indexManagerScheduleInterval = (long) (settings.getSettingAsFloat(LuceneEnvironment.SearchEngineIndex.INDEX_MANAGER_SCHEDULE_INTERVAL, 60.0f) * 1000);
        if (log.isDebugEnabled()) {
            log.debug("Using index maanger schedule interval [" + indexManagerScheduleInterval + "ms]");
        }
    }

    public String getAllProperty() {
        return allProperty;
    }

    public void setAllProperty(String allProperty) {
        this.allProperty = allProperty;
    }

    public String getAliasProperty() {
        return aliasProperty;
    }

    public void setAliasProperty(String aliasProperty) {
        this.aliasProperty = aliasProperty;
    }

    public TransactionIsolation getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(TransactionIsolation transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public Class getTransactionIsolationClass() {
        return transactionIsolationClass;
    }

    public void setTransactionIsolationClass(Class transactionIsolationClass) {
        this.transactionIsolationClass = transactionIsolationClass;
    }

    public int getMaxMergeDocs() {
        return maxMergeDocs;
    }

    public void setMaxMergeDocs(int maxMergeDocs) {
        this.maxMergeDocs = maxMergeDocs;
    }

    public int getMergeFactor() {
        return mergeFactor;
    }

    public void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    public boolean isUseCompoundFile() {
        return useCompoundFile;
    }

    public void setUseCompoundFile(boolean useCompoundFile) {
        this.useCompoundFile = useCompoundFile;
    }

    public int getMaxFieldLength() {
        return maxFieldLength;
    }

    public void setMaxFieldLength(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
    }

    public int getMaxBufferedDocs() {
        return maxBufferedDocs;
    }

    public void setMaxBufferedDocs(int maxBufferedDocs) {
        this.maxBufferedDocs = maxBufferedDocs;
    }

    public String getDefaultSearchPropery() {
        return defaultSearchPropery;
    }

    public void setDefaultSearchPropery(String defaultSearchPropery) {
        this.defaultSearchPropery = defaultSearchPropery;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public Property.TermVector getAllPropertyTermVector() {
        return allPropertyTermVector;
    }

    public void setAllPropertyTermVector(Property.TermVector allPropertyTermVector) {
        this.allPropertyTermVector = allPropertyTermVector;
    }

    public long getTransactionLockTimout() {
        return transactionLockTimout;
    }

    public void setTransactionLockTimout(long transactionLockTimout) {
        this.transactionLockTimout = transactionLockTimout;
    }

    public long getTransactionCommitTimeout() {
        return transactionCommitTimeout;
    }

    public void setTransactionCommitTimeout(long transactionCommitTimeout) {
        this.transactionCommitTimeout = transactionCommitTimeout;
    }

    public long getCacheInvalidationInterval() {
        return cacheInvalidationInterval;
    }

    public void setCacheInvalidationInterval(long cacheInvalidationInterval) {
        this.cacheInvalidationInterval = cacheInvalidationInterval;
    }

    public String getLockDir() {
        return lockDir;
    }

    public long getIndexManagerScheduleInterval() {
        return indexManagerScheduleInterval;
    }
}
