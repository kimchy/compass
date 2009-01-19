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

package org.compass.needle.gigaspaces.store;

import java.io.IOException;

import com.j_spaces.core.IJSpace;
import net.jini.core.lease.Lease;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

/**
 * A locak factory using a {@link FileLock} entry
 * as a mark that a certain lock is obtained.
 *
 * @author kimchy
 */
public class GigaSpaceLockFactory extends LockFactory {

    private static final Log log = LogFactory.getLog(GigaSpaceLockFactory.class);

    private IJSpace space;

    private String indexName;

    public GigaSpaceLockFactory(IJSpace space, String indexName) {
        this.space = space;
        this.indexName = indexName;
    }

    public void clearLock(String lockName) throws IOException {
        try {
            space.clear(new FileLock(indexName, lockName), null);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, lockName, "Failed to clear lock", e);
        }
    }

    public Lock makeLock(String lockName) {
        return new GigaSpaceLock(lockName);
    }

    public class GigaSpaceLock extends Lock {

        private FileLock fileLock;

        public GigaSpaceLock(String lockName) {
            this.fileLock = new FileLock(indexName, lockName);
        }

        public boolean isLocked() {
            try {
                int count = space.count(fileLock, null);
                return count > 0;
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to check if object is locked on index [" + indexName + "]", e);
                }
            }
            return false;
        }

        public boolean obtain() throws IOException {
            try {
                space.write(fileLock, null, Lease.FOREVER);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public void release() {
            try {
                space.clear(fileLock, null);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to release locke on index [" + indexName + "]", e);
                }
            }
        }
    }
}
