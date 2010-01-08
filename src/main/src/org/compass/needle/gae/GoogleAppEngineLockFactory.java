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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.compass.needle.gae.GoogleAppEngineDirectory.Callable;

/**
 * @author kimchy
 */
public class GoogleAppEngineLockFactory extends LockFactory {

    private static final String LOCK_KIND = "lock";

    private static final Log log = LogFactory.getLog(GoogleAppEngineLockFactory.class);

    private final GoogleAppEngineDirectory dir;

    public GoogleAppEngineLockFactory(GoogleAppEngineDirectory dir) {
        this.dir = dir;
    }

    public Lock makeLock(String lockName) {
        return new GoogleAppEngineLock(lockName);
    }

    public void clearLock(String lockName) throws IOException {
        new GoogleAppEngineLock(lockName).doRelease();
    }

    public class GoogleAppEngineLock extends Lock {

        private final Entity lock;

        public GoogleAppEngineLock(String lockName) {
            lockName += "-" + dir.getIndexName();
            this.lock = new Entity(LOCK_KIND, lockName);
        }

        public boolean isLocked() {

            try {
                // Does not force a transaction, but uses a transaction only if
                // necessary.
                return dir.doInTransaction(new Callable<Boolean>() {

                    @Override
                    public Boolean call(Transaction transaction) throws Exception {
                        try {
                            dir.getDatastoreService().get(transaction, lock.getKey());
                            return true;
                        } catch (EntityNotFoundException ex) {
                            return false;
                        }
                    }

                });
            } catch (GoogleAppEngineDirectoryException e) {

                if (log.isWarnEnabled()) {
                    log.warn("Failed to check if object is locked on index [" + dir.getIndexName() + "]", e);
                }

                return false;
            }

        }

        public boolean obtain() throws IOException {
            final int attempts = dir.getTransactionRetryCount();

            try {
                // Locking needs to be done in a transaction of its own because
                // the get and subsequent set are not atomic operations with the
                // datastore.
                return dir.doInTransaction(attempts, true, new Callable<Boolean>() {

                    @Override
                    public Boolean call(Transaction transaction) throws Exception {
                        try {
                            dir.getDatastoreService().get(transaction, lock.getKey());
                            return false;
                        } catch (EntityNotFoundException enfex) {
                            dir.getDatastoreService().put(transaction, lock);
                            return true;
                        }
                    }

                });
            } catch (GoogleAppEngineAttemptsExpiredException ex) {
                // Trying to obtain the lock failed several times. Give up and
                // return false.
                return false;
            }
        }

        public void release() {

            try {
                doRelease();
            } catch (GoogleAppEngineDirectoryException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to release lock on index [" + dir.getIndexName() + "]", ex);
                }
            }

        }

        private void doRelease() throws GoogleAppEngineDirectoryException {
            dir.doInTransaction(1, true, new Callable<Void>() {

                @Override
                public Void call(Transaction transaction) throws Exception {
                    dir.getDatastoreService().delete(transaction, lock.getKey());
                    return null;
                }

            });

        }

    }

}
