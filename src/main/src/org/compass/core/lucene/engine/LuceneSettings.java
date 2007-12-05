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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.RAMTransLog;
import org.apache.lucene.index.TransLog;
import org.apache.lucene.store.Lock;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.Property;
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

    private CompassSettings settings;

    private String connection;

    private String subContext;

    private String defaultSearchPropery;

    private String allProperty;

    private Property.TermVector allPropertyTermVector;

    private String aliasProperty;

    private String extendedAliasProperty;

    private TransactionIsolation transactionIsolation;

    private Class transactionIsolationClass;

    private int maxMergeDocs;

    private int mergeFactor;

    private boolean useCompoundFile;

    private int maxFieldLength;

    private int maxBufferedDocs;

    private long transactionLockTimout;

    private long cacheInvalidationInterval;

    private long indexManagerScheduleInterval;

    private boolean waitForCacheInvalidationOnIndexOperation;

    private String lockDir;

    public void configure(CompassSettings settings) throws SearchEngineException {
        this.settings = settings;
        connection = settings.getSetting(CompassEnvironment.CONNECTION);
        if (connection == null) {
            throw new SearchEngineException("Lucene connection must be set in the settings. Please set ["
                    + CompassEnvironment.CONNECTION + "]");
        }
        subContext = settings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT);
        if (log.isDebugEnabled()) {
            log.debug("Using connection [" + connection + "][" + subContext + "]");
        }
        // the alias property
        aliasProperty = settings.getSetting(CompassEnvironment.Alias.NAME, CompassEnvironment.Alias.DEFAULT_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Using alias property [" + aliasProperty + "]");
        }

        extendedAliasProperty = settings.getSetting(CompassEnvironment.Alias.EXTENDED_ALIAS_NAME, CompassEnvironment.Alias.DEFAULT_EXTENDED_ALIAS_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Using extended alias property [" + extendedAliasProperty + "]");
        }

        // get the all property
        allProperty = settings.getSetting(CompassEnvironment.All.NAME, CompassEnvironment.All.DEFAULT_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Using default all property [" + allProperty + "]");
        }
        String allPropertyTermVectorSettings = settings.getSetting(CompassEnvironment.All.TERM_VECTOR, "no");
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
        Lock.LOCK_POLL_INTERVAL = settings.getSettingAsLong(LuceneEnvironment.Transaction.LOCK_POLL_INTERVAL, 100);
        if (log.isDebugEnabled()) {
            log.debug("Using lock poll interval [" + Lock.LOCK_POLL_INTERVAL + "ms]");
        }

        lockDir = settings.getSetting("compass.transaction.lockDir");
        if (lockDir != null) {
            throw new IllegalArgumentException("compass.transaction.lockDir setting is no longer supported. " +
                    "The lock by default is stored in the index directory now, and can be conrolled by using LockFactory");
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
            log.debug("Using index manager schedule interval [" + indexManagerScheduleInterval + "ms]");
        }

        waitForCacheInvalidationOnIndexOperation = settings.getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.WAIT_FOR_CACHE_INVALIDATION_ON_INDEX_OPERATION, false);
        if (log.isDebugEnabled()) {
            log.debug("Wait for cahce invalidation on index operatrion is set to [" + waitForCacheInvalidationOnIndexOperation + "]");
        }
    }

    public TransLog createTransLog(CompassSettings settings) {
        if (settings == null) {
            settings = this.settings;
        }
        TransLog transLog;
        try {
            Class transLogClass = settings.getSettingAsClass(LuceneEnvironment.Transaction.TransLog.TYPE, RAMTransLog.class);
            if (log.isTraceEnabled()) {
                log.trace("Using Trans Log [" + transLogClass.getName() + "]");
            }
            transLog = (TransLog) transLogClass.newInstance();
        } catch (Exception e) {
            throw new SearchEngineException("Failed to create transLog", e);
        }
        transLog.configure(settings);
        return transLog;
    }

    public CompassSettings getSettings() {
        return this.settings;
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

    public String getExtendedAliasProperty() {
        return extendedAliasProperty;
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

    public boolean isWaitForCacheInvalidationOnIndexOperation() {
        return waitForCacheInvalidationOnIndexOperation;
    }

    public String getSubContext() {
        return subContext;
    }
}
