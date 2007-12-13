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

import com.j_spaces.core.IJSpace;
import net.jini.core.lease.Lease;
import org.apache.lucene.store.BufferedIndexOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.openspaces.core.GigaSpace;

/**
 * GigaSpace Directory is a Lucene directory built on top of GigaSpaces.
 *
 * <p>The direcotry implementation uses {@link FileEntry}
 * as the meta data of a file written to the Space. And one or more {@link FileBucketEntry}
 * to hold the entry data. Note, the bucket size can eb controlled during index creation, but if connecting
 * to an existing index, the bucket index must be the same.
 *
 * @author kimchy
 */
public class GigaSpaceDirectory extends Directory {

    public static final int DEFAULT_BUCKET_SIZE = 20 * 1024;

    private IJSpace space;

    private String indexName;

    private int bucketSize = DEFAULT_BUCKET_SIZE;

    public GigaSpaceDirectory(GigaSpace gigaSpace, String indexName) {
        this(gigaSpace.getSpace(), indexName);
    }

    public GigaSpaceDirectory(IJSpace space, String indexName) {
        this(space, indexName, DEFAULT_BUCKET_SIZE);
    }

    public GigaSpaceDirectory(IJSpace space, String indexName, int bucketSize) {
        this.space = space;
        this.indexName = indexName;
        this.bucketSize = bucketSize;
        setLockFactory(new GigaSpaceLockFactory(space, indexName));
    }

    public void deleteContent() throws IOException {
        FileEntry fileEntry = new FileEntry(indexName, null);
        try {
            space.clear(fileEntry, null);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, null, "Delete failed", e);
        }
        FileBucketEntry fileBucketEntry = new FileBucketEntry(indexName, null);
        try {
            space.clear(fileBucketEntry, null);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, null, "Delete failed", e);
        }
    }

    public void deleteFile(String fileName) throws IOException {
        FileEntry fileEntry = new FileEntry(indexName, fileName);
        try {
            space.clear(fileEntry, null);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "Delete failed", e);
        }
        FileBucketEntry fileBucketEntry = new FileBucketEntry(indexName, fileName);
        try {
            space.clear(fileBucketEntry, null);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "Delete failed", e);
        }
    }

    public boolean fileExists(String fileName) throws IOException {
        FileEntry fileEntry = new FileEntry(indexName, fileName);
        try {
            int count = space.count(fileEntry, null);
            return count > 0;
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "File exists failed", e);
        }
    }

    public long fileLength(String fileName) throws IOException {
        FileEntry fileEntryTemplate = new FileEntry(indexName, fileName);
        try {
            FileEntry fileEntry = (FileEntry) space.read(fileEntryTemplate, null, 0);
            if (fileEntry == null) {
                return 0;
            }
            return fileEntry.getSize();
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "File length failed", e);
        }
    }

    public long fileModified(String fileName) throws IOException {
        FileEntry fileEntryTemplate = new FileEntry(indexName, fileName);
        try {
            FileEntry fileEntry = (FileEntry) space.read(fileEntryTemplate, null, 0);
            if (fileEntry == null) {
                return 0;
            }
            return fileEntry.getLastModified();
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "File modified failed", e);
        }
    }

    public String[] list() throws IOException {
        FileEntry fileEntryTemplate = new FileEntry(indexName, null);
        try {
            Object[] results = space.readMultiple(fileEntryTemplate, null, Integer.MAX_VALUE);
            String[] retVal = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                retVal[i] = ((FileEntry) results[i]).getFileName();
            }
            return retVal;
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, null, "File list failed", e);
        }
    }

    public void renameFile(String from, String to) throws IOException {
        // renameFile is not used within Lucene anymore
        throw new UnsupportedOperationException("rename file not supported with GigaSpace direcotry");
    }

    public void touchFile(String fileName) throws IOException {
        FileEntry fileEntryTemplate = new FileEntry(indexName, fileName);
        try {
            FileEntry fileEntry = (FileEntry) space.take(fileEntryTemplate, null, 0);
            if (fileEntry == null) {
                fileEntry = new FileEntry(indexName, fileName, 0);
            }
            fileEntry.touch();
            space.write(fileEntry, null, Lease.FOREVER);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "File touch failed", e);
        }
    }

    public IndexInput openInput(String fileName) throws IOException {
        FileEntry fileEntry = new FileEntry(indexName, fileName);
        try {
            fileEntry = (FileEntry) space.read(fileEntry, null, 0);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "File to read file entry", e);
        }
        if (fileEntry == null) {
            throw new GigaSpaceDirectoryException(indexName, fileName, "Failed to find file entry");
        }
        return new GigaSpaceIndexInput(fileEntry);
    }

    public IndexOutput createOutput(String fileName) throws IOException {
        return new GigaSpaceIndexOutput(fileName);
    }

    /**
     * Does nothing since an already constructed Space is passes to this
     * directory.
     */
    public void close() throws IOException {
    }

    /**
     * @author kimchy
     */
    private class GigaSpaceIndexInput extends IndexInput {

        private FileEntry fileEntry;

        private long position;

        private FileBucketEntry bucketEntry;

        private int currentBucketPosition;

        public GigaSpaceIndexInput(FileEntry fileEntry) {
            this.fileEntry = fileEntry;
            this.bucketEntry = new FileBucketEntry(fileEntry.indexName, fileEntry.fileName, -1, null);
        }

        public void close() throws IOException {
        }

        /**
         * Returns the current position in this file, where the next read will
         * occur.
         *
         * @see #seek(long)
         */
        public long getFilePointer() {
            return this.position;
        }

        /**
         * The number of bytes in the file.
         */
        public long length() {
            return fileEntry.getSize();
        }

        /**
         * Reads and returns a single byte.
         *
         * @see IndexOutput#writeByte(byte)
         */
        public byte readByte() throws IOException {
            loadBucketIfNeeded();
            position++;
            return bucketEntry.data[currentBucketPosition++];
        }

        /**
         * Reads a specified number of bytes into an array at the specified
         * offset.
         *
         * @param b      the array to read bytes into
         * @param offset the offset in the array to start storing bytes
         * @param len    the number of bytes to read
         * @see IndexOutput#writeBytes(byte[],int)
         */
        public void readBytes(byte[] b, int offset, int len) throws IOException {
            loadBucketIfNeeded();
            // if there is enough place to load at once
            if (len <= (bucketSize - currentBucketPosition)) {
                if (len > 0) {
                    System.arraycopy(bucketEntry.data, currentBucketPosition, b, offset, len);
                }
                currentBucketPosition += len;
                position += len;
                return;
            }

            // cycle through the reads
            while (true) {
                int available = bucketSize - currentBucketPosition;
                int sizeToRead = (len <= available) ? len : available;
                System.arraycopy(bucketEntry.data, currentBucketPosition, b, offset, sizeToRead);
                len -= sizeToRead;
                offset += sizeToRead;
                position += sizeToRead;
                currentBucketPosition += sizeToRead;
                // check if we read enough, if we did, bail
                if (len <= 0) {
                    break;
                }
                loadBucketIfNeeded();
            }
        }

        /**
         * Sets current position in this file, where the next read will occur.
         *
         * @see #getFilePointer()
         */
        public void seek(long pos) throws IOException {
            position = pos;
        }

        private void loadBucketIfNeeded() throws GigaSpaceDirectoryException {
            currentBucketPosition = (int) position % bucketSize;
            long bucketIndex = position / bucketSize;
            // check if we need to load the bucket
            if (bucketIndex == bucketEntry.bucketIndex) {
                return;
            }
            // reuse the current bucket entry as the template
            bucketEntry.data = null;
            bucketEntry.bucketIndex = bucketIndex;
            try {
                bucketEntry = (FileBucketEntry) space.read(bucketEntry, null, 0);
            } catch (Exception e) {
                throw new GigaSpaceDirectoryException(fileEntry.indexName, fileEntry.fileName,
                        "Failed to read bucket [" + bucketIndex + "]", e);
            }
            if (bucketEntry == null) {
                throw new GigaSpaceDirectoryException(fileEntry.indexName, fileEntry.fileName, "Bucket [" + bucketIndex
                        + "] not found");
            }
        }
    }

    /**
     * @author kimchy
     */
    // TODO Performance improvement: for small files, just write to a single
    // bucket entry instead of a file
    private class GigaSpaceIndexOutput extends BufferedIndexOutput {

        private File tempFile;

        private RandomAccessFile file = null;

        private String fileName;

        // remember if the file is open, so that we don't try to close it
        // more than once
        private boolean isOpen;

        public GigaSpaceIndexOutput(String fileName) throws IOException {
            this.fileName = fileName;
            tempFile = File.createTempFile(indexName + "_" + fileName + "_" + System.currentTimeMillis(), ".lucene-gigaspacesdir");
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
                    deleteFile(fileName);
                }

                // go over the file and write it to the space
                file.seek(0);
                byte[] buffer = new byte[bucketSize];

                // TODO consider using transactions to wrap all the writes
                int offset = 0;
                int length = bucketSize;
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
                        flushBucket(bucketIndex++, buffer, bucketSize);
                        offset = 0;
                        length = bucketSize;
                    }
                }
                try {
                    space.write(new FileEntry(indexName, fileName, file.length()), null, Lease.FOREVER);
                } catch (Exception e) {
                    throw new GigaSpaceDirectoryException(indexName, fileName, "Failed to write file entry", e);
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
            FileBucketEntry fileBucketEntry = new FileBucketEntry(indexName, fileName, bucketIndex, null);
            fileBucketEntry.data = new byte[length];
            System.arraycopy(buffer, 0, fileBucketEntry.data, 0, length);
            try {
                space.write(fileBucketEntry, null, Lease.FOREVER);
            } catch (Exception e) {
                throw new GigaSpaceDirectoryException(indexName, fileName, "Failed to write bucket [" + bucketIndex
                        + "]", e);
            }
        }
    }
}
