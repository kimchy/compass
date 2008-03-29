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

import com.tc.object.bytecode.ManagerUtil;
import com.tc.object.lockmanager.api.LockLevel;
import org.apache.lucene.store.Lock;

/**
 * @author kimchy
 */
class TerracottaManagerUtilLock extends Lock {

    private String lockName;

    public TerracottaManagerUtilLock(String lockName) {
        this.lockName = lockName;
    }

    public boolean obtain() throws IOException {
        return ManagerUtil.tryBeginLock(lockName, LockLevel.WRITE);
    }

    public void release() {
        try {
            ManagerUtil.commitLock(lockName);
        } catch (Exception e) {
            // we get this exception if this is not locked
        }
    }

    public boolean isLocked() {
        return ManagerUtil.isLocked(lockName, LockLevel.WRITE);
    }

    public String toString() {
        return "TerracottaManagerUtilLock: " + lockName;
    }
}