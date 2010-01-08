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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.apache.lucene.store.IndexInput;

/**
 * @author kimchy
 */
public class GoogleAppEngineIndexInput extends IndexInput {

    private final GoogleAppEngineDirectory dir;

    private final Entity metaDataEntity;

    private final boolean useMemcache;

    private long position;

    private long currentBucketIndex = -1;

    private byte[] currentBucketData;

    private int currentBucketPosition;

    public GoogleAppEngineIndexInput(GoogleAppEngineDirectory dir, Entity metaDataEntity, boolean useMemcache) {
        this.dir = dir;
        this.metaDataEntity = metaDataEntity;
        this.useMemcache = useMemcache;
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
        return (Long) metaDataEntity.getProperty("size");
    }

    /**
     * Reads and returns a single byte.
     *
     * @see org.apache.lucene.store.IndexOutput#writeByte(byte)
     */
    public byte readByte() throws IOException {
        loadBucketIfNeeded();
        position++;
        return currentBucketData[currentBucketPosition++];
    }

    /**
     * Reads a specified number of bytes into an array at the specified offset.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param len    the number of bytes to read
     * @see org.apache.lucene.store.IndexOutput#writeBytes(byte[],int)
     */
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        loadBucketIfNeeded();
        // if there is enough place to load at once
        if (len <= (dir.getBucketSize() - currentBucketPosition)) {
            if (len > 0) {
                System.arraycopy(currentBucketData, currentBucketPosition, b, offset, len);
            }
            currentBucketPosition += len;
            position += len;
            return;
        }

        // cycle through the reads
        while (true) {
            int available = dir.getBucketSize() - currentBucketPosition;
            int sizeToRead = (len <= available) ? len : available;
            System.arraycopy(currentBucketData, currentBucketPosition, b, offset, sizeToRead);
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

    private void loadBucketIfNeeded() throws GoogleAppEngineDirectoryException {
        currentBucketPosition = (int) position % dir.getBucketSize();
        long bucketIndex = position / dir.getBucketSize();
        // check if we need to load the bucket
        if (bucketIndex == currentBucketIndex) {
            return;
        }

        Key key = KeyFactory.createKey(metaDataEntity.getKey(), GoogleAppEngineDirectory.CONTENT_KEY_KIND,
                metaDataEntity.getKey().getName() + bucketIndex);
        if (useMemcache) {
            currentBucketData = (byte[]) dir.getMemcacheService().get(key);
            if (currentBucketData == null) {
                Entity bucketEntity = dir.getEntity(key);
                currentBucketData = ((Blob) bucketEntity.getProperty("data")).getBytes();
                dir.getMemcacheService().put(key, currentBucketData);
            }
        } else {
            Entity bucketEntity = dir.getEntity(key);
            currentBucketData = ((Blob) bucketEntity.getProperty("data")).getBytes();
        }
        currentBucketIndex = bucketIndex;
    }

    public Object clone() {
        GoogleAppEngineIndexInput indexInput = (GoogleAppEngineIndexInput) super.clone();
        indexInput.currentBucketIndex = -1;
        indexInput.currentBucketData = null;
        return indexInput;
    }
}
