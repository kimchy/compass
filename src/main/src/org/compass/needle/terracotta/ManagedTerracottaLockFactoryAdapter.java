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
import org.apache.lucene.store.LockFactory;

/**
 * @author kimchy
 */
public class ManagedTerracottaLockFactoryAdapter extends LockFactory {

    private final ReadWriteLock rwl;

    private final LockFactory lockFactory;

    public ManagedTerracottaLockFactoryAdapter(ReadWriteLock rwl, LockFactory lockFactory) {
        this.rwl = rwl;
        this.lockFactory = lockFactory;
    }

    public Lock makeLock(String lockName) {
        return new ManagedTerracottaLockAdapter(rwl, lockFactory.makeLock(lockName));
    }

    public void clearLock(String lockName) throws IOException {
        lockFactory.clearLock(lockName);
    }
}
