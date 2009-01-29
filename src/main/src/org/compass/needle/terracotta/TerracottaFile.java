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

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kimchy
 */
final class TerracottaFile {

    private ArrayList<BufferWrapper> buffers = new ArrayList<BufferWrapper>();

    long length;

    private long lastModified = System.currentTimeMillis();

    private ReentrantLock lock = new ReentrantLock();

    TerracottaFile() {
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    // Not syncronize on get/set Length, should be ok, but needs to be verified
    long getLength() {
        return length;
    }

    void setLength(long length) {
        this.length = length;
    }

    synchronized long getLastModified() {
        return lastModified;
    }

    synchronized void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    // no need to sync on buffer operations, since all operations are locked on the
    // global lock (lock/unlock) and then it is only reads.
    final byte[] addBuffer(int size) {
        BufferWrapper buffer = new BufferWrapper(new byte[size]);
        buffers.add(buffer);
        return buffer.buffer.buffer;
    }

    // same hear, no need to sync
    final void addBuffers(byte[] ... newBuffers) {
        for (byte[] buffer : newBuffers) {
            buffers.add(new BufferWrapper(buffer));
        }
    }

    final void setFirstBuffer(byte[] buffer) {
        buffers.get(0).buffer.buffer = buffer;
    }

    final byte[] getBuffer(int index) {
        return buffers.get(index).buffer.buffer;
    }

    final int getNumBuffers() {
        return buffers.size();
    }

    public static class BufferWrapper {
        public final Buffer buffer;

        private BufferWrapper(byte[] buffer) {
            this.buffer = new Buffer(buffer);
        }

        public byte[] getBuffer() {
            return buffer.buffer;
        }
    }

    public static class Buffer {
        public byte[] buffer;

        private Buffer(byte[] buffer) {
            this.buffer = buffer;
        }
    }
}
