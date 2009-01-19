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

package org.compass.needle.terracotta;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * An adapter ontop of a lock that releases the managed read lock when trying to obtain
 * a Lucene lock and obtains the read lock again once the Lucene lock obtain is done.
 * It is done in order to prevent dead locks when several clients/threads obtain the read lock
 * when the "transaction" starts, and they wait for the lucene lock without actually releasing the
 * read lock for other "write locks (the add file)" to be able to be obtained.
 *
 * @author kimchy
 */
public class ManagedTerracottaLockAdapter extends Lock {

    private final ReadWriteLock rwl;

    private final Lock lock;

    public ManagedTerracottaLockAdapter(ReadWriteLock rwl, Lock lock) {
        this.rwl = rwl;
        this.lock = lock;
    }

    @Override
    public boolean obtain(long lockWaitTimeout) throws LockObtainFailedException, IOException {
        boolean unlocked;
        try {
            rwl.readLock().unlock();
            unlocked = true;
        } catch (IllegalMonitorStateException e) {
            unlocked = false;
        }
        try {
            return lock.obtain(lockWaitTimeout);
        } finally {
            if (unlocked) {
                rwl.readLock().lock();
            }
        }
    }

    @Override
    public boolean obtain() throws IOException {
        boolean unlocked;
        try {
            rwl.readLock().unlock();
            unlocked = true;
        } catch (IllegalMonitorStateException e) {
            unlocked = false;
        }
        try {
            return lock.obtain();
        } finally {
            if (unlocked) {
                rwl.readLock().lock();
            }
        }
    }

    @Override
    public void release() throws IOException {
        lock.release();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }
}
