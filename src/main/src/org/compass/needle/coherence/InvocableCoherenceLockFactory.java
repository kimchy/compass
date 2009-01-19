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

package org.compass.needle.coherence;

import java.io.IOException;

import com.tangosol.net.NamedCache;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

/**
 * A lock factory using Coherence invocable support in order to use the actual
 * existance of a key within the cache to represent a lock.
 *
 * @author kimchy
 */
public class InvocableCoherenceLockFactory extends LockFactory {

    private static final Log log = LogFactory.getLog(InvocableCoherenceLockFactory.class);

    private NamedCache cache;

    private String indexName;

    public InvocableCoherenceLockFactory(NamedCache cache, String indexName) {
        this.cache = cache;
        this.indexName = indexName;
    }

    public void clearLock(String lockName) throws IOException {
        cache.remove(new FileLockKey(indexName, lockName));
    }

    public Lock makeLock(String lockName) {
        return new CoherenceLock(lockName);
    }

    public class CoherenceLock extends Lock {

        private FileLockKey fileLock;

        public CoherenceLock(String lockName) {
            this.fileLock = new FileLockKey(indexName, lockName);
        }

        public boolean isLocked() {
            return cache.containsKey(fileLock);
        }

        public boolean obtain() throws IOException {
            Integer isLocked = (Integer) cache.invoke(fileLock, new ConditionalPut(new NotFilter(PresentFilter.INSTANCE), 1, true));
            return isLocked == null;
        }

        public void release() {
            try {
                cache.remove(fileLock);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to release lock on index [" + indexName + "]", e);
                }
            }
        }
    }
}