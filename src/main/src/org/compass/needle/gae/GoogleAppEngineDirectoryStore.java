/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.needle.gae;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.store.AbstractDirectoryStore;
import org.compass.core.lucene.engine.store.CopyFromHolder;
import org.compass.core.util.StringUtils;

/**
 * A plugin lucene store for Compass. Uses {@link GoogleAppEngineDirectory} as
 * Lucene directory implementation.
 *
 * @author kimchy
 */
public class GoogleAppEngineDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    public static final String PROTOCOL = "gae://";

    public static final String BUCKET_SIZE_PROP = "compass.engine.store.gae.bucketSize";

    public static final String FLUSH_RATE_PROP = "compass.engine.store.gae.flushRate";

    /**
     * The GAE documentation recommends retrying transactions several times when
     * attempting to maniuplate the datastore. Where appropraite, the directory
     * story will attempt transactions this many times. Defaults to 3.
     */
    public static final String TRANSACTION_RETRY_COUNT_PROP = "compass.engine.store.gae.transactionRetryCount";

    /**
     * Should the directory cache file meta data instead of fetching it from GAE
     * each time. Defatuls to <code>true</code>.
     */
    public static final String CACHE_META_DATA_PROP = "compass.engine.store.gae.cacheMetaData";

    /**
     * File names based patterns to use in order to choose which Lucene files to
     * store in memcahe and which not. Lucene files are explained here:
     * http://lucene.apache.org/java/2_4_1/fileformats.html.
     */
    public static final String MEMCACHE_REGEX_PATTERNS_PROP = "compass.engine.store.gae.memcacheRegexPatterns";

    private String indexName;

    private int bucketSize;

    private int flushRate;

    private boolean cacheMetaData;

    private int transactionRetryCount;

    private String[] memcaheRegexPatterns;

    public void configure(CompassSettings settings) throws CompassException {
        this.indexName = settings.getSetting(CompassEnvironment.CONNECTION).substring(PROTOCOL.length());

        bucketSize = (int) settings.getSettingAsBytes(BUCKET_SIZE_PROP, GoogleAppEngineDirectory.DEFAULT_BUCKET_SIZE);
        flushRate = settings.getSettingAsInt(FLUSH_RATE_PROP, GoogleAppEngineDirectory.DEFAULT_FLUSH_RATE);
        cacheMetaData = settings.getSettingAsBoolean(CACHE_META_DATA_PROP, GoogleAppEngineDirectory.DEFAULT_CACHE_META_DATA);
        transactionRetryCount = settings.getSettingAsInt(TRANSACTION_RETRY_COUNT_PROP, GoogleAppEngineDirectory.DEFAULT_TRANSACTION_RETRY_COUNT);

        String memcaheRegexPatternsConf = settings.getSetting(MEMCACHE_REGEX_PATTERNS_PROP);

        if (memcaheRegexPatternsConf != null) {
            memcaheRegexPatterns = StringUtils.commaDelimitedListToStringArray(memcaheRegexPatternsConf);
        } else {
            memcaheRegexPatterns = new String[0];
        }

    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        return new GoogleAppEngineDirectory(buildFullIndexName(subContext, subIndex), bucketSize, flushRate, cacheMetaData, transactionRetryCount, memcaheRegexPatterns);
    }

    @Override
    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        cleanIndex(dir, subContext, subIndex);
    }

    @Override
    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {

        try {
            ((GoogleAppEngineDirectory) dir).deleteContent();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete index for sub context [" + subContext + "] and sub index [" + subIndex + "]", e);
        }

    }

    @Override
    public CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir)
            throws SearchEngineException {

        try {
            ((GoogleAppEngineDirectory) dir).deleteContent();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete context before copy from", e);
        }

        return new CopyFromHolder();
    }

    private String buildFullIndexName(String subContext, String subIndex) {
        return indexName + "-" + subContext + "-" + subIndex;
    }

    @Override
    public String suggestedIndexDeletionPolicy() {
        return LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.NAME;
    }

}