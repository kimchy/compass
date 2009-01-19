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

package org.apache.lucene.store.wrapper;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.RAMOutputStream;

/**
 * Wraps a Lucene {@link org.apache.lucene.store.Directory} with
 * an in memory directory which mirrors it synchronously.
 * <p/>
 * The original directory is read into memory when this wrapper
 * is constructed. All read realted operations are performed
 * against the in memory directory. All write related operations
 * are performed both against the in memeory directory and the
 * original directory. Locking is performed using the in memory
 * directory.
 * <p/>
 * NOTE: This wrapper will only work in cases when either the
 * index is read only (i.e. only search operations are performed
 * against it), or when there is a single instance which updates
 * the directory.
 *
 * @author kimchy
 */
public class SyncMemoryMirrorDirectoryWrapper extends Directory {

    private Directory dir;

    private RAMDirectory ramDir;

    public SyncMemoryMirrorDirectoryWrapper(Directory dir) throws IOException {
        this.dir = dir;
        this.ramDir = new RAMDirectory(dir);
    }

    public void deleteFile(String name) throws IOException {
        ramDir.deleteFile(name);
        dir.deleteFile(name);
    }

    public boolean fileExists(String name) throws IOException {
        return ramDir.fileExists(name);
    }

    public long fileLength(String name) throws IOException {
        return ramDir.fileLength(name);
    }

    public long fileModified(String name) throws IOException {
        return ramDir.fileModified(name);
    }

    public String[] list() throws IOException {
        return ramDir.list();
    }

    public void renameFile(String from, String to) throws IOException {
        ramDir.renameFile(from, to);
        dir.renameFile(from, to);
    }

    public void touchFile(String name) throws IOException {
        ramDir.touchFile(name);
        dir.touchFile(name);
    }

    public Lock makeLock(String name) {
        return ramDir.makeLock(name);
    }

    public void close() throws IOException {
        ramDir.close();
        dir.close();
    }

    public IndexInput openInput(String name) throws IOException {
        return ramDir.openInput(name);
    }

    public IndexOutput createOutput(String name) throws IOException {
        return new SyncMemoryMirrorIndexOutput(dir.createOutput(name), (RAMOutputStream) ramDir.createOutput(name));
    }

    public static class SyncMemoryMirrorIndexOutput extends IndexOutput {

        private IndexOutput origIndexOutput;

        private RAMOutputStream ramIndexOutput;

        public SyncMemoryMirrorIndexOutput(IndexOutput origIndexOutput, RAMOutputStream ramIndexOutput) {
            this.origIndexOutput = origIndexOutput;
            this.ramIndexOutput = ramIndexOutput;
        }

        public void writeByte(byte b) throws IOException {
            ramIndexOutput.writeByte(b);
        }

        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            ramIndexOutput.writeBytes(b, offset, length);
        }

        public void seek(long size) throws IOException {
            ramIndexOutput.seek(size);
        }

        public long length() throws IOException {
            return ramIndexOutput.length();
        }

        public long getFilePointer() {
            return ramIndexOutput.getFilePointer();
        }

        public void flush() throws IOException {
            ramIndexOutput.flush();
        }

        public void close() throws IOException {
            ramIndexOutput.close();
            ramIndexOutput.writeTo(origIndexOutput);
            origIndexOutput.close();
        }
    }
}
