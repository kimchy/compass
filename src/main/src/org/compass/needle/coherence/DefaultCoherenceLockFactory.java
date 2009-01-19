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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * A locak factory using.
 *
 * @author kimchy
 */
public class DefaultCoherenceLockFactory extends LockFactory {

    private static final Log log = LogFactory.getLog(DefaultCoherenceLockFactory.class);

    private NamedCache cache;

    private String indexName;

    public DefaultCoherenceLockFactory(NamedCache cache, String indexName) {
        this.cache = cache;
        this.indexName = indexName;
    }

    public void clearLock(String lockName) throws IOException {
        cache.unlock(new FileLockKey(indexName, lockName));
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
            // TOOD how to we really check if something is locked?
            return false;
        }

        public boolean obtain() throws IOException {
            return cache.lock(fileLock);
        }

        public boolean obtain(long lockWaitTimeout) throws LockObtainFailedException, IOException {
            return cache.lock(fileLock, lockWaitTimeout);
        }

        public void release() {
            try {
                cache.unlock(fileLock);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to release locke on index [" + indexName + "]", e);
                }
            }
        }
    }
}