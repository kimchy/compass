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
import java.util.ArrayList;
import java.util.Collections;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.IndexOutput;
import org.compass.needle.gae.GoogleAppEngineDirectory.Callable;

/**
 * @author kimchy
 */
class GoogleAppEngineMemIndexOutput extends IndexOutput {

    private final GoogleAppEngineDirectory dir;

    private final String fileName;

    private final Key metaDataKey;

    private Entity firstBucketEntity;

    private byte[] buffer;

    private int bufferPosition;

    private int currentBucketIndex;

    private long length;

    private long position;

    private boolean open;

    // seek occured, we only allow to work on the first bucket
    private boolean seekOccured;

    private final ArrayList<Entity> flushEntities;

    public GoogleAppEngineMemIndexOutput(GoogleAppEngineDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        open = true;
        buffer = new byte[dir.getBucketSize()];
        // this file is overridden by Lucene, so delete it first
        if (LuceneFileNames.isStaticFile(fileName)) {
            dir.deleteFile(fileName);
        }
        flushEntities = new ArrayList<Entity>(dir.getFlushRate());
        this.metaDataKey = dir.getIndexKey().getChild(GoogleAppEngineDirectory.META_KEY_KIND, fileName);
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == dir.getBucketSize()) {
            if (seekOccured) {
                throw new GoogleAppEngineDirectoryException(dir.getIndexName(), fileName,
                        "Seek occured and overflowed first bucket");
            }
            flushBucket();
        }
        buffer[bufferPosition++] = b;
        if (!seekOccured) {
            length++;
            position++;
        }
    }

    public void writeBytes(byte[] b, int offset, int len) throws IOException {
        if (!seekOccured) {
            position += len;
            length += len;
        }
        while (len > 0) {
            if (bufferPosition == dir.getBucketSize()) {
                if (seekOccured) {
                    throw new GoogleAppEngineDirectoryException(dir.getIndexName(), fileName,
                            "Seek occured and overflowed first bucket");
                }
                flushBucket();
            }

            int remainInBuffer = dir.getBucketSize() - bufferPosition;
            int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
            System.arraycopy(b, offset, buffer, bufferPosition, bytesToCopy);
            offset += bytesToCopy;
            len -= bytesToCopy;
            bufferPosition += bytesToCopy;
        }
    }

    public void flush() throws IOException {
        // do nothing here
    }

    public void close() throws IOException {
        if (!open) {
            return;
        }
        open = false;
        try {
            // flush any bucket we might have
            flushBucket();

            Entity metaDataEntity = new Entity(GoogleAppEngineDirectory.META_KEY_KIND, fileName, dir.getIndexKey());
            metaDataEntity.setProperty("size", length);
            metaDataEntity.setProperty("modified", System.currentTimeMillis());

            forceFlushBuckets(firstBucketEntity, metaDataEntity);
            dir.addMetaData(metaDataEntity);
            buffer = null;
            firstBucketEntity = null;
        } finally {
            dir.getOnGoingIndexOutputs().remove(fileName);
        }
    }

    public long getFilePointer() {
        return this.position;
    }

    public void seek(long pos) throws IOException {
        if (pos >= dir.getBucketSize()) {
            throw new GoogleAppEngineDirectoryException(dir.getIndexName(), fileName,
                    "seek called outside of first bucket boundries");
        }
        // create the first bucket if still not created
        if (firstBucketEntity == null) {
            firstBucketEntity = new Entity(GoogleAppEngineDirectory.CONTENT_KEY_KIND, fileName + 0, metaDataKey);
            byte[] data = new byte[bufferPosition];
            System.arraycopy(buffer, 0, data, 0, bufferPosition);
            firstBucketEntity.setProperty("data", new Blob(data));
        } else {
            // flush the current buffer. We only seek into the first bucket
            // so no need to keep it around
            if (!seekOccured) {
                flushBucket(currentBucketIndex, buffer, bufferPosition);
            }
        }
        position = pos;
        currentBucketIndex = 0;
        bufferPosition = (int) pos;
        buffer = ((Blob) firstBucketEntity.getProperty("data")).getBytes();
        seekOccured = true;
    }

    public long length() throws IOException {
        return length;
    }

    private void flushBucket() throws IOException {
        if (currentBucketIndex == 0) {
            if (firstBucketEntity == null) {
                firstBucketEntity = new Entity(GoogleAppEngineDirectory.CONTENT_KEY_KIND, fileName + 0, metaDataKey);
                byte[] data = new byte[bufferPosition];
                System.arraycopy(buffer, 0, data, 0, bufferPosition);
                firstBucketEntity.setProperty("data", new Blob(data));
            } else {
                // do nothing, we are writing directly into the first buffer
            }
        } else {
            if (bufferPosition > 0) {
                flushBucket(currentBucketIndex, buffer, bufferPosition);
            }
        }
        currentBucketIndex++;
        bufferPosition = 0;
    }

    private void flushBucket(long bucketIndex, byte[] buffer, int length) throws IOException {
        Entity fileBucketEntity = new Entity(GoogleAppEngineDirectory.CONTENT_KEY_KIND, fileName + bucketIndex,
                metaDataKey);
        byte[] data = new byte[length];
        System.arraycopy(buffer, 0, data, 0, length);
        fileBucketEntity.setProperty("data", new Blob(data));
        flushEntities.add(fileBucketEntity);
        if (flushEntities.size() >= dir.getFlushRate()) {
            //What's going on here?
            forceFlushBuckets();
        }
    }

    private void forceFlushBuckets(Entity... additionalEntities) throws IOException {

        if (flushEntities.size() == 0 && additionalEntities == null) {
            return;
        }

        Collections.addAll(flushEntities, additionalEntities);

        try {
            dir.doInTransaction(new Callable<Void>() {

                @Override
                public Void call(Transaction transaction) throws Exception {
                    dir.getDatastoreService().put(transaction, flushEntities);
                    return null;
                }

            });
        } finally {
            flushEntities.clear();
        }
    }
}
