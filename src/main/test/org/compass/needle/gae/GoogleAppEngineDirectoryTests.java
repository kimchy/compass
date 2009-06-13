/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.needle.gae;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * @author kimchy
 */
public class GoogleAppEngineDirectoryTests extends LocalServiceTestCase {

    public void test1BufferMemcache() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 1, new String[] {".*"});
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test3BufferMemcache() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 3, new String[] {".*"});
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test10BufferMemcache() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 10, new String[] {".*"});
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test15BufferMemcache() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 15, new String[] {".*"});
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test40BufferMemcache() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 40, new String[] {".*"});
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test1Buffer() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 1);
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test3Buffer() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 3);
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test10Buffer() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 10);
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test15Buffer() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 15);
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test40Buffer() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 40);
        insertData(dir, "value1");
        verifyData(dir, "value1");
    }

    public void test1BufferFlushOnClose() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 1);
        insertData(dir, "segments");
        verifyData(dir, "segments");
    }

    public void test3BufferFlushOnClose() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 3);
        insertData(dir, "segments");
        verifyData(dir, "segments");
    }

    public void test10BufferFlushOnClose() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 10);
        insertData(dir, "segments");
        verifyData(dir, "segments");
    }

    public void test15BufferFlushOnClose() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 15);
        insertData(dir, "segments");
        verifyData(dir, "segments");
    }

    public void test40BufferFlushOnClose() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 40);
        insertData(dir, "segments");
        verifyData(dir, "segments");
    }

    public void testSimpeLocking() throws Exception {
        GoogleAppEngineDirectory dir = new GoogleAppEngineDirectory("test", 40);
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

    private void insertData(GoogleAppEngineDirectory dir, String fileName) throws IOException {
        byte[] test = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        IndexOutput indexOutput = dir.createOutput(fileName);
        indexOutput.writeBytes(new byte[]{2, 4, 6, 7, 8}, 5);
        indexOutput.writeInt(-1);
        indexOutput.writeLong(10);
        indexOutput.writeInt(0);
        indexOutput.writeInt(0);
        indexOutput.writeBytes(test, 8);
        indexOutput.writeBytes(test, 5);

        indexOutput.seek(0);
        indexOutput.writeByte((byte) 8);
        if (dir.getBucketSize() > 4) {
            indexOutput.seek(2);
            indexOutput.writeBytes(new byte[]{1, 2}, 2);
        }

        indexOutput.close();
    }

    private void verifyData(GoogleAppEngineDirectory dir, String fileName) throws IOException {
        byte[] test = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertTrue(dir.fileExists(fileName));
        assertEquals(38, dir.fileLength(fileName));

        IndexInput indexInput = dir.openInput(fileName);
        indexInput.readBytes(test, 0, 5);
        assertEquals(8, test[0]);
        assertEquals(-1, indexInput.readInt());
        assertEquals(10, indexInput.readLong());
        assertEquals(0, indexInput.readInt());
        assertEquals(0, indexInput.readInt());
        indexInput.readBytes(test, 0, 8);
        assertEquals((byte) 1, test[0]);
        assertEquals((byte) 8, test[7]);
        indexInput.readBytes(test, 0, 5);
        assertEquals((byte) 1, test[0]);
        assertEquals((byte) 5, test[4]);

        indexInput.seek(28);
        assertEquals((byte) 4, indexInput.readByte());
        indexInput.seek(30);
        assertEquals((byte) 6, indexInput.readByte());

        indexInput.close();

        dir.deleteFile(fileName);
        assertFalse(dir.fileExists(fileName));
    }
}