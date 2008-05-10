package org.compass.needle.coherence;

/**
 * @author kimchy
 */
public class DefaultCoherenceDirectoryTests extends AbstractCoherenceDirectoryTests {

    protected CoherenceDirectory doCreateDirectory(String name, int bucketSize) {
        return new DataGridCoherenceDirectory(getCache(), name, bucketSize);
    }
}
