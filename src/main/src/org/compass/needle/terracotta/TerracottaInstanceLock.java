/*
 * Copyright 2004-2006 the original author or authors.
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
import java.util.HashSet;

import org.apache.lucene.store.Lock;

/**
 * @author kimchy
 */
class TerracottaInstanceLock extends Lock {

    private String lockName;
    private final HashSet<String> locks;

    public TerracottaInstanceLock(HashSet<String> locks, String lockName) {
        this.locks = locks;
        this.lockName = lockName;
    }

    public boolean obtain() throws IOException {
        synchronized (locks) {
            return locks.add(lockName);
        }
    }

    public void release() {
        synchronized (locks) {
            locks.remove(lockName);
        }
    }

    public boolean isLocked() {
        synchronized (locks) {
            return locks.contains(lockName);
        }
    }

    public String toString() {
        return "TerracottaInstanceLock: " + lockName;
    }
}
