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

package org.compass.needle.gigaspaces.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.jini.core.lease.Lease;
import org.apache.lucene.store.BufferedIndexOutput;

/**
 * Writes the output to a file and then when closed will flush it to the Space.
 *
 * <p>Not optimial, but allows for random seeking outside of just the first bucket.
 * Not used since Lucene only seeks to the first buffer (with proper bucket size).
 *
 * @author kimchy
 */
class GigaSpaceFileIndexOutput extends BufferedIndexOutput {

    private GigaSpaceDirectory dir;

    private File tempFile;

    private RandomAccessFile file = null;

    private String fileName;

    // remember if the file is open, so that we don't try to close it
    // more than once
    private boolean isOpen;

    public GigaSpaceFileIndexOutput(GigaSpaceDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        tempFile = File.createTempFile(dir.getIndexName() + "_" + fileName + "_" + System.currentTimeMillis(), ".lucene-gigaspacesdir");
        this.file = new RandomAccessFile(tempFile, "rw");
        isOpen = true;
    }

    /**
     * output methods:
     */
    public void flushBuffer(byte[] b, int offset, int size) throws IOException {
        file.write(b, offset, size);
    }

    public void close() throws IOException {
        // only close the file if it has not been closed yet
        if (isOpen) {
            super.close();

            // this file is overridden by Lucene, so delete it first
            if (fileName.equals("segments.gen")) {
                dir.deleteFile(fileName);
            }

            // go over the file and write it to the space
            file.seek(0);
            byte[] buffer = new byte[dir.getBucketSize()];

            // TODO consider using transactions to wrap all the writes
            int offset = 0;
            int length = dir.getBucketSize();
            long bucketIndex = 0;
            while (true) {
                int sizeRead = file.read(buffer, offset, length);
                if (sizeRead == -1) {
                    flushBucket(bucketIndex, buffer, offset);
                    break;
                }
                offset += sizeRead;
                length -= sizeRead;
                if (length <= 0) {
                    flushBucket(bucketIndex++, buffer, dir.getBucketSize());
                    offset = 0;
                    length = dir.getBucketSize();
                }
            }
            try {
                dir.getSpace().write(new FileEntry(dir.getIndexName(), fileName, file.length()), null, Lease.FOREVER);
            } catch (Exception e) {
                throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Failed to write file entry", e);
            }
            file.close();
            tempFile.delete();
            isOpen = false;
        }
    }

    /**
     * Random-access methods
     */
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public long length() throws IOException {
        return file.length();
    }

    private void flushBucket(long bucketIndex, byte[] buffer, int length) throws IOException {
        FileBucketEntry fileBucketEntry = new FileBucketEntry(dir.getIndexName(), fileName, bucketIndex, null);
        fileBucketEntry.data = new byte[length];
        System.arraycopy(buffer, 0, fileBucketEntry.data, 0, length);
        try {
            dir.getSpace().write(fileBucketEntry, null, Lease.FOREVER);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Failed to write bucket [" + bucketIndex
                    + "]", e);
        }
    }
}
