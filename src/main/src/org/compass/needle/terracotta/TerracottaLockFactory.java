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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

/**
 * A lock factory that creates {@link org.compass.needle.terracotta.TerracottaLock}s. Uses a concurrent
 * hash map to hold the locks.
 *
 * @author kimchy
 */
public class TerracottaLockFactory extends LockFactory {

    static final Object MARK = new Object();

    private final String prefix;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<String, Object>();

    public TerracottaLockFactory() {
        this("");
    }

    public TerracottaLockFactory(String prefix) {
        this.prefix = prefix;
    }

    public Lock makeLock(String lockName) {
        // We do not use the LockPrefix at all, because the private
        // HashSet instance effectively scopes the locking to this
        // single Directory instance.
        return new TerracottaLock(locks, prefix + lockName);
    }

    public void clearLock(String lockName) throws IOException {
        locks.remove(lockName);
    }
}

