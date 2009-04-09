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
import java.util.ConcurrentModificationException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

/**
 * @author kimchy
 */
public class GoogleAppEngineLockFactory extends LockFactory {

    private static final Log log = LogFactory.getLog(GoogleAppEngineLockFactory.class);

    private final GoogleAppEngineDirectory dir;

    public GoogleAppEngineLockFactory(GoogleAppEngineDirectory dir) {
        this.dir = dir;
    }

    public Lock makeLock(String lockName) {
        return new GoogleAppEngineLock(lockName);
    }

    public void clearLock(String lockName) throws IOException {
        dir.getDatastoreService().delete(dir.getIndexKey().getChild("lock", makeLockName(lockName, dir.getIndexName())));
    }

    private String makeLockName(String lockName, String indexName) {
        // TODO remove the index name in the next release
        return indexName + "-" + lockName;
    }

    public class GoogleAppEngineLock extends Lock {

        private final Entity lock;

        public GoogleAppEngineLock(String lockName) {
            this.lock = new Entity("lock", makeLockName(lockName, dir.getIndexName()), dir.getIndexKey());
        }

        public boolean isLocked() {
            try {
                try {
                    dir.getDatastoreService().get(lock.getKey());
                    return true;
                } catch (EntityNotFoundException e) {
                    return false;
                }
            } catch (Exception e1) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to check if object is locked on index [" + dir.getIndexName() + "]", e1);
                }
            }
            return false;
        }

        public boolean obtain() throws IOException {
            Transaction transaction = dir.getDatastoreService().beginTransaction();
            try {
                dir.getDatastoreService().get(transaction, lock.getKey());
                transaction.commit();
                return false;
            } catch (EntityNotFoundException e) {
                // no lock, continue with trying to create one
            }
            try {
                dir.getDatastoreService().put(transaction, lock);
                transaction.commit();
                return true;
            } catch (ConcurrentModificationException e) {
                // someone has created an Entity with the same Key in between get and put
                transaction.commit();
                return false;
            }
        }

        public void release() {
            try {
                Transaction transaction = dir.getDatastoreService().beginTransaction();
                dir.getDatastoreService().delete(transaction, lock.getKey());
                transaction.commit();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to release lock on index [" + dir.getIndexName() + "]", e);
                }
            }
        }
    }

}
