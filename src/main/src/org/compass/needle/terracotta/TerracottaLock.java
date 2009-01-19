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

/**
 * @author kimchy
 */
class TerracottaLock extends Lock {

    private String lockName;
    
    private final ConcurrentHashMap<String, Object> locks;

    public TerracottaLock(ConcurrentHashMap<String, Object> locks, String lockName) {
        this.locks = locks;
        this.lockName = lockName;
    }

    public boolean obtain() throws IOException {
        return locks.putIfAbsent(lockName, TerracottaLockFactory.MARK) == null;
    }

    public void release() {
        locks.remove(lockName);
    }

    public boolean isLocked() {
        return locks.containsKey(lockName);
    }

    public String toString() {
        return "TerracottaLock: " + lockName;
    }
}
