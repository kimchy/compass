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