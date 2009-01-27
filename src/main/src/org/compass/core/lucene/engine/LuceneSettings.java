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

package org.compass.core.lucene.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Lock;
import org.compass.core.Property;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

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

    private boolean allPropertyBoostSupport;

    private String aliasProperty;

    private String extendedAliasProperty;

    private int maxMergeDocs;

    private int mergeFactor;

    private int maxFieldLength;

    private int maxBufferedDocs;

    private int maxBufferedDeletedTerms;

    private int termIndexInterval;

    private double ramBufferSize;

    private long transactionLockTimout;

    private long cacheInvalidationInterval;

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
        allPropertyBoostSupport = settings.getSettingAsBoolean(CompassEnvironment.All.BOOST_SUPPORT, true);
        if (log.isDebugEnabled()) {
            log.debug("All property boost support is [" + allPropertyBoostSupport + "]");
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
        // lucene specifics parameters
        transactionLockTimout = settings.getSettingAsTimeInSeconds(LuceneEnvironment.Transaction.LOCK_TIMEOUT, 10) * 1000;
        if (log.isDebugEnabled()) {
            log.debug("Using transaction lock timeout [" + transactionLockTimout + "ms]");
        }
        IndexWriter.setDefaultWriteLockTimeout(transactionLockTimout);

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

        // pure lucene transaction settings
        mergeFactor = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, 10);
        maxBufferedDocs = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, IndexWriter.DISABLE_AUTO_FLUSH);
        maxBufferedDeletedTerms = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DELETED_TERMS, IndexWriter.DISABLE_AUTO_FLUSH);
        termIndexInterval = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.TERM_INDEX_INTERVAL, IndexWriter.DEFAULT_TERM_INDEX_INTERVAL);
        maxFieldLength = settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_FIELD_LENGTH, IndexWriter.DEFAULT_MAX_FIELD_LENGTH);

        ramBufferSize = settings.getSettingAsDouble(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB);

        // cach invalidation settings
        cacheInvalidationInterval = settings.getSettingAsTimeInMillis(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, LuceneEnvironment.SearchEngineIndex.DEFAULT_CACHE_INTERVAL_INVALIDATION);
        if (log.isDebugEnabled()) {
            log.debug("Using cache invalidation interval [" + cacheInvalidationInterval + "ms]");
        }

        waitForCacheInvalidationOnIndexOperation = settings.getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.WAIT_FOR_CACHE_INVALIDATION_ON_INDEX_OPERATION, false);
        if (log.isDebugEnabled()) {
            log.debug("Wait for cahce invalidation on index operatrion is set to [" + waitForCacheInvalidationOnIndexOperation + "]");
        }

        BooleanQuery.setMaxClauseCount(settings.getSettingAsInt(LuceneEnvironment.Query.MAX_CLAUSE_COUNT, BooleanQuery.getMaxClauseCount()));
        if (log.isDebugEnabled()) {
            log.debug("Setting *static* Lucene BooleanQuery maxClauseCount to [" + BooleanQuery.getMaxClauseCount() + "]");
        }
    }

    public CompassSettings getSettings() {
        return this.settings;
    }

    public String getAllProperty() {
        return allProperty;
    }

    public String getAliasProperty() {
        return aliasProperty;
    }

    public String getExtendedAliasProperty() {
        return extendedAliasProperty;
    }

    public int getMaxMergeDocs() {
        return maxMergeDocs;
    }

    public int getMergeFactor() {
        return mergeFactor;
    }

    public int getMaxFieldLength() {
        return maxFieldLength;
    }

    public int getMaxBufferedDocs() {
        return maxBufferedDocs;
    }

    public int getMaxBufferedDeletedTerms() {
        return maxBufferedDeletedTerms;
    }

    public int getTermIndexInterval() {
        return termIndexInterval;
    }

    public double getRamBufferSize() {
        return ramBufferSize;
    }

    public String getDefaultSearchPropery() {
        return defaultSearchPropery;
    }

    public String getConnection() {
        return connection;
    }

    public Property.TermVector getAllPropertyTermVector() {
        return allPropertyTermVector;
    }

    public boolean isAllPropertyBoostSupport() {
        return allPropertyBoostSupport;
    }

    public long getTransactionLockTimout() {
        return transactionLockTimout;
    }

    public long getCacheInvalidationInterval() {
        return cacheInvalidationInterval;
    }

    public String getLockDir() {
        return lockDir;
    }

    public boolean isWaitForCacheInvalidationOnIndexOperation() {
        return waitForCacheInvalidationOnIndexOperation;
    }

    public String getSubContext() {
        return subContext;
    }
}
