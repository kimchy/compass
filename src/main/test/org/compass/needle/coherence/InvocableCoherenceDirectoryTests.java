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

package org.compass.needle.coherence;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * @author kimchy
 */
public class InvocableCoherenceDirectoryTests extends AbstractCoherenceDirectoryTests {

    protected CoherenceDirectory doCreateDirectory(String name, int bucketSize) {
        return new InvocableCoherenceDirectory(getCache(), name, bucketSize);
    }

    public void testSimpeLocking() throws Exception {
        CoherenceDirectory dir = doCreateDirectory("test", 40);
        Lock lock = dir.makeLock("testlock");
        assertFalse(lock.isLocked());
        assertTrue(lock.obtain(2000));
        assertTrue(lock.isLocked());
        try {
            assertFalse(lock.obtain(2000));
            fail();
        } catch (LockObtainFailedException e) {
            // all is well
        }
        lock.release();
        assertFalse(lock.isLocked());
    }

}