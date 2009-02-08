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

package org.compass.core.cache.first;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class FirstLevelCacheFactory implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(FirstLevelCacheFactory.class);

    private Class firstLevelCacheClass;

    private boolean nullFirstLevelCache;

    public void configure(CompassSettings settings) throws CompassException {
        String transactionProcessorName = settings.getSetting(LuceneEnvironment.Transaction.Processor.TYPE, null);
        if (transactionProcessorName != null
                && (transactionProcessorName.equalsIgnoreCase(LuceneEnvironment.Transaction.Processor.Lucene.NAME))) {
            firstLevelCacheClass = NullFirstLevelCache.class;
        } else {
            String firstLevelCacheSetting = settings.getSetting(CompassEnvironment.Cache.FirstLevel.TYPE, NullFirstLevelCache.class.getName());
            try {
                firstLevelCacheClass = ClassUtils.forName(firstLevelCacheSetting, settings.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Failed to find class name [" + firstLevelCacheSetting
                        + "] for first level cache", e);
            }
        }
        nullFirstLevelCache = firstLevelCacheClass.equals(NullFirstLevelCache.class);
        if (log.isDebugEnabled()) {
            log.debug("Using first level cache [" + firstLevelCacheClass.getName() + "]");
        }
    }

    public FirstLevelCache createFirstLevelCache() throws CompassException {
        if (nullFirstLevelCache) {
            return NullFirstLevelCache.INSTANCE;
        }
        try {
            return (FirstLevelCache) firstLevelCacheClass.newInstance();
        } catch (Exception e) {
            throw new CompassException("Failed to create first level cache", e);
        }
    }
}
