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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.lucene.store.Lock;

/**
 * Managed terracotta directory allows to use the terracotta directory by managing a more coarse
 * grained transactions and operations against the directory. This should provide an improved
 * performance with terracotta since there is no need to obtain locks on a fine grained level
 * (CHM) within a transactional context.
 *
 * <p>The directory accepts a RWL, which should be managed by terracotta. In order to begin a
 * transaction, the RWL read lock should be obtained. Releasing the transaction should be performed
 * by unlocking the read lock.
 *
 * @author kimchy
 */
public class ManagedTerracottaDirectory extends TerracottaDirectory {

    private final ReadWriteLock rwl;

    public ManagedTerracottaDirectory(ReadWriteLock rwl) {
        super();
        this.rwl = rwl;
    }

    public ManagedTerracottaDirectory(ReadWriteLock rwl, int bufferSize, int flushRate) {
        super(bufferSize, flushRate);
        this.rwl = rwl;
    }

    public ManagedTerracottaDirectory(ReadWriteLock rwl, int bufferSize, int flushRate, int chmInitialCapacity, float chmLoadFactor, int chmConcurrencyLevel) {
        super(bufferSize, flushRate, chmInitialCapacity, chmLoadFactor, chmConcurrencyLevel);
        this.rwl = rwl;
    }

    @Override
    protected Map<String, TerracottaFile> createMap(int chmInitialCapacity, float chmLoadFactor, int chmConcurrencyLevel) {
        return new HashMap<String, TerracottaFile>(chmInitialCapacity, chmLoadFactor);
    }

    @Override
    public Lock makeLock(String name) {
        return new ManagedTerracottaLockAdapter(rwl, super.makeLock(name));
    }

    @Override
    public void deleteFile(final String name) throws IOException {
        doWithWriteLock(new WriteLockTask<Object>() {
            public Object execute() throws IOException {
                ManagedTerracottaDirectory.super.deleteFile(name);
                return null;
            }
        });
    }

    @Override
    public void renameFile(final String from, final String to) throws IOException {
        doWithWriteLock(new WriteLockTask<Object>() {
            public Object execute() throws IOException {
                ManagedTerracottaDirectory.super.renameFile(from, to);
                return null;
            }
        });
    }

    @Override
    void addFile(final String name, final TerracottaFile file) throws IOException {
        doWithWriteLock(new WriteLockTask<Object>() {
            public Object execute() throws IOException {
                ManagedTerracottaDirectory.super.addFile(name, file);
                return null;
            }
        });
    }

    protected <T> T doWithWriteLock(WriteLockTask<T> task) throws IOException {
        boolean unlocked;
        try {
            rwl.readLock().unlock();
            unlocked = true;
        } catch (IllegalMonitorStateException e) {
            unlocked = false;
        }
        rwl.writeLock().lock();
        try {
            return task.execute();
        } finally {
            if (unlocked) {
                rwl.readLock().lock();
            }
            rwl.writeLock().unlock();
        }
    }

    private static interface WriteLockTask<T> {
        T execute() throws IOException;
    }
}
