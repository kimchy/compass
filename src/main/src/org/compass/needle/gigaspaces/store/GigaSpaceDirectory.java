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

package org.compass.needle.gigaspaces.store;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.j_spaces.core.IJSpace;
import net.jini.core.lease.Lease;
import org.apache.lucene.index.LuceneFileNames;
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

    public static final int DEFAULT_FLUSH_RATE = 50;

    private IJSpace space;

    private String indexName;

    private int bucketSize = DEFAULT_BUCKET_SIZE;

    private int flushRate = DEFAULT_FLUSH_RATE;

    // we store an on going list of created index outputs since Lucene needs them
    // *before* it closes the index output. It calls fileExists in the middle.
    private Map<String, IndexOutput> onGoingIndexOutputs = new ConcurrentHashMap<String, IndexOutput>();

    public GigaSpaceDirectory(GigaSpace gigaSpace, String indexName) {
        this(gigaSpace.getSpace(), indexName);
    }

    public GigaSpaceDirectory(IJSpace space, String indexName) {
        this(space, indexName, DEFAULT_BUCKET_SIZE);
    }

    public GigaSpaceDirectory(IJSpace space, String indexName, int bucketSize) {
        this(space, indexName, bucketSize, DEFAULT_FLUSH_RATE);
    }

    public GigaSpaceDirectory(IJSpace space, String indexName, int bucketSize, int flushRate) {
        this.space = space;
        this.indexName = indexName;
        this.bucketSize = bucketSize;
        this.flushRate = flushRate;
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

    Map<String, IndexOutput> getOnGoingIndexOutputs() {
        return onGoingIndexOutputs;
    }

    public IJSpace getSpace() {
        return space;
    }

    String getIndexName() {
        return indexName;
    }

    int getBucketSize() {
        return bucketSize;
    }

    int getFlushRate() {
        return flushRate;
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
        if (onGoingIndexOutputs.containsKey(fileName)) {
            return true;
        }
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
        return new GigaSpaceIndexInput(this, fileEntry);
    }

    public IndexOutput createOutput(String fileName) throws IOException {
        IndexOutput out;
        if (LuceneFileNames.isSegmentsFile(fileName)) {
            out = new FlushOnCloseGigaSpaceIndexOutput(this, fileName);
        } else {
            out = new GigaSpaceMemIndexOutput(this, fileName);
        }
        onGoingIndexOutputs.put(fileName, out);
        return out;
    }

    /**
     * Does nothing since an already constructed Space is passes to this
     * directory.
     */
    public void close() throws IOException {
    }

}
