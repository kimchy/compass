package org.compass.gps.device.support.parallel;

import junit.framework.TestCase;

/**
 * @author kimchy
 */
public class SubIndexIndexEntitiesPartitionerTests extends TestCase {

    public class MockIndexEntity implements IndexEntity {

        private String[] subIndexes;

        public MockIndexEntity(String[] subIndexes) {
            this.subIndexes = subIndexes;
        }

        public String[] getSubIndexes() {
            return subIndexes;
        }

        public String getName() {
            return "";
        }
    }

    public void testSimplePartition() throws Exception {
        IndexEntity[] entities = new IndexEntity[]{
                new MockIndexEntity(new String[]{"a", "b"}),
                new MockIndexEntity(new String[]{"c", "d"})
        };
        SubIndexIndexEntitiesPartitioner partitioner = new SubIndexIndexEntitiesPartitioner();
        IndexEntity[][] parEnt = partitioner.partition(entities);
        assertEquals(2, parEnt.length);
    }

    public void testSamePartition() throws Exception {
        IndexEntity[] entities = new IndexEntity[]{
                new MockIndexEntity(new String[]{"a", "b"}),
                new MockIndexEntity(new String[]{"a", "b"})
        };
        SubIndexIndexEntitiesPartitioner partitioner = new SubIndexIndexEntitiesPartitioner();
        IndexEntity[][] parEnt = partitioner.partition(entities);
        assertEquals(1, parEnt.length);
    }

    public void testJoinedPartition() throws Exception {
        IndexEntity[] entities = new IndexEntity[]{
                new MockIndexEntity(new String[]{"a", "b"}),
                new MockIndexEntity(new String[]{"b", "c"}),
                new MockIndexEntity(new String[]{"d", "c"})
        };
        SubIndexIndexEntitiesPartitioner partitioner = new SubIndexIndexEntitiesPartitioner();
        IndexEntity[][] parEnt = partitioner.partition(entities);
        assertEquals(1, parEnt.length);
        assertEquals(3, parEnt[0].length);
    }

    public void testJoinedPartitionAndOneWithoutAtTheEnd() throws Exception {
        IndexEntity[] entities = new IndexEntity[]{
                new MockIndexEntity(new String[]{"a", "b"}),
                new MockIndexEntity(new String[]{"b", "c"}),
                new MockIndexEntity(new String[]{"d", "c"}),
                new MockIndexEntity(new String[]{"e", "f"})
        };
        SubIndexIndexEntitiesPartitioner partitioner = new SubIndexIndexEntitiesPartitioner();
        IndexEntity[][] parEnt = partitioner.partition(entities);
        assertEquals(2, parEnt.length);
        assertEquals(3, parEnt[0].length);
        assertEquals(1, parEnt[1].length);
    }

    public void testJoinedPartitionAndOneWithoutAtTheBeginning() throws Exception {
        IndexEntity[] entities = new IndexEntity[]{
                new MockIndexEntity(new String[]{"x", "y"}),
                new MockIndexEntity(new String[]{"a", "b"}),
                new MockIndexEntity(new String[]{"b", "c"}),
                new MockIndexEntity(new String[]{"d", "c"}),
                new MockIndexEntity(new String[]{"e", "f"})
        };
        SubIndexIndexEntitiesPartitioner partitioner = new SubIndexIndexEntitiesPartitioner();
        IndexEntity[][] parEnt = partitioner.partition(entities);
        assertEquals(3, parEnt.length);
        assertEquals(1, parEnt[0].length);
        assertEquals(3, parEnt[1].length);
        assertEquals(1, parEnt[2].length);
    }

}
