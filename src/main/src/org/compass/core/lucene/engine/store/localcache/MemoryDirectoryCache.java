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

package org.compass.core.lucene.engine.store.localcache;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexFileNameFilter;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.DirectoryWrapper;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.transaction.context.TransactionContextCallback;
import org.compass.core.util.StringUtils;
import org.compass.core.util.concurrent.ConcurrentHashSet;
import org.compass.core.util.concurrent.ConcurrentLinkedHashMap;

/**
 * Evictable Memory based directory cache wrapping the actual Lucene Directory the index uses. Accepts
 * connection string parameters: <code>bucketSize</code> which is the size of each bucket entry (defaults
 * to 1024 bytes), <code>size</code> which controls the maximum amount of memory that will be taken by
 * the cache before entries will start to be evicted (defaults to <code>64m</code>), and <code>cacheFileNames</code>
 * that controls if file names will try to be cached as well (defaults to <code>true</code>). 
 *
 * @author kimchy
 */
public class MemoryDirectoryCache extends Directory implements DirectoryWrapper {

    private static final Log logger = LogFactory.getLog(MemoryDirectoryCache.class);

    private final Directory dir;

    private final LocalCacheManager localCacheManager;

    private final Set<String> localFileNames = new ConcurrentHashSet<String>();

    private final boolean cacheFileNames;

    private final ConcurrentLinkedHashMap<CacheKey, byte[]> cache;

    private final int bucketSize;

    private ScheduledFuture cleanupTaskFuture;

    private final boolean isCompoundFile;

    public MemoryDirectoryCache(String connectionString, Directory dir, LocalCacheManager localCacheManager) {
        this.dir = dir;
        this.localCacheManager = localCacheManager;
        String[] args = StringUtils.delimitedListToStringArray(connectionString, "&");
        int bucketSize = 1024;
        long size = CompassSettings.parseStringAsBytes("64m");
        boolean cacheFileNames = true;
        for (String arg : args) {
            if (arg.startsWith("bucketSize=")) {
                bucketSize = (int) CompassSettings.parseStringAsBytes(arg.substring("bucketSize=".length()));
            } else if (arg.startsWith("size=")) {
                size = CompassSettings.parseStringAsBytes(arg.substring("size=".length()));
            } else if (arg.startsWith("cacheFileNames=")) {
                cacheFileNames = Boolean.parseBoolean(arg.substring("cacheFileNames=".length()));
            }
        }
        this.cacheFileNames = cacheFileNames;
        this.bucketSize = bucketSize;
        
        int numberOfCacheEntries = (int) (size / bucketSize);
        this.cache = new ConcurrentLinkedHashMap<CacheKey, byte[]>(ConcurrentLinkedHashMap.EvictionPolicy.SECOND_CHANCE, numberOfCacheEntries);

        if (localCacheManager == null) {
            isCompoundFile = false; // just cache everything
        } else {
            isCompoundFile = localCacheManager.getSearchEngineFactory().getLuceneIndexManager().getStore().isUseCompoundFile();
        }

        if (cacheFileNames) {
            cleanupTaskFuture = localCacheManager.getSearchEngineFactory().getExecutorManager().scheduleWithFixedDelay(new CleanupTask(), 10, 10, TimeUnit.SECONDS);
        }
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public Directory getWrappedDirectory() {
        return this.dir;
    }

    public void clearWrapper() throws IOException {
        cache.clear();
    }

    public String[] list() throws IOException {
        return dir.list();
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        if (!cacheFileNames) {
            return dir.fileExists(name);
        }
        if (shouldPerformOperationOnActualDirectory(name)) {
            return dir.fileExists(name);
        }
        if (localFileNames.contains(name)) {
            return true;
        }
        boolean fileExists = dir.fileExists(name);
        if (fileExists) {
            localFileNames.add(name);
        }
        return fileExists;
    }

    @Override
    public long fileModified(String name) throws IOException {
        return dir.fileModified(name);
    }

    @Override
    public void touchFile(String name) throws IOException {
        dir.touchFile(name);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        dir.deleteFile(name);
    }

    @Override
    public void renameFile(String from, String to) throws IOException {
        dir.renameFile(from, to);
    }

    @Override
    public long fileLength(String name) throws IOException {
        return dir.fileLength(name);
    }

    @Override
    public void close() throws IOException {
        if (cacheFileNames) {
            cleanupTaskFuture.cancel(true);
        }
        cache.clear();
        localFileNames.clear();
    }

    @Override
    public Lock makeLock(String name) {
        return dir.makeLock(name);
    }

    @Override
    public void clearLock(String name) throws IOException {
        dir.clearLock(name);
    }

    @Override
    public void setLockFactory(LockFactory lockFactory) {
        dir.setLockFactory(lockFactory);
    }

    @Override
    public LockFactory getLockFactory() {
        return dir.getLockFactory();
    }

    @Override
    public String getLockID() {
        return dir.getLockID();
    }

    @Override
    public IndexOutput createOutput(String name) throws IOException {
        return new WrappedIndexOutput(name, dir.createOutput(name));
    }

    @Override
    public IndexInput openInput(String name) throws IOException {
        if (shouldWrapInput(name)) {
            return new WrappedIndexInput(name);
        }
        return dir.openInput(name);
    }

    @Override
    public IndexInput openInput(String name, int bufferSize) throws IOException {
        if (shouldWrapInput(name)) {
            return new WrappedIndexInput(name);
        }
        return dir.openInput(name, bufferSize);
    }

    private boolean shouldWrapInput(String name) {
        if (shouldPerformOperationOnActualDirectory(name)) {
            return false;
        }
        if (isCompoundFile && IndexFileNameFilter.getFilter().isCFSFile(name)) {
            return false;
        }
        return true;
    }

    private boolean shouldPerformOperationOnActualDirectory(String name) {
        return LuceneFileNames.isStaticFile(name);
    }

    private class CleanupTask implements Runnable {
        public void run() {
            String[] fileNames = localCacheManager.getSearchEngineFactory().getTransactionContext().execute(new TransactionContextCallback<String[]>() {
                public String[] doInTransaction() throws CompassException {
                    try {
                        return dir.list();
                    } catch (IOException e) {
                        logger.error("Failed to list file names", e);
                        return null;
                    }
                }
            });
            if (fileNames == null) {
                return;
            }
            localFileNames.clear();
            for (String fileName : fileNames) {
                localFileNames.add(fileName);
            }
        }
    }

    private class WrappedIndexOutput extends IndexOutput {

        private final String fileName;

        private final IndexOutput indexOutput;

        private WrappedIndexOutput(String fileName, IndexOutput indexOutput) {
            this.fileName = fileName;
            this.indexOutput = indexOutput;
        }

        public void writeByte(byte b) throws IOException {
            indexOutput.writeByte(b);
        }

        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            indexOutput.writeBytes(b, offset, length);
        }

        public void flush() throws IOException {
            indexOutput.flush();
        }

        public void close() throws IOException {
            indexOutput.close();
            localFileNames.add(fileName);
        }

        public long getFilePointer() {
            return indexOutput.getFilePointer();
        }

        public void seek(long pos) throws IOException {
            indexOutput.seek(pos);
        }

        public long length() throws IOException {
            return indexOutput.length();
        }
    }

    private class WrappedIndexInput extends IndexInput {

        private final String fileName;

        private IndexInput indexInput;

        private long currentPos = 0;

        private WrappedIndexInput(String fileName) throws IOException {
            this.fileName = fileName;
            this.indexInput = dir.openInput(fileName, 1); // no need for any buffer size, we read fully from source in chunks
        }

        public byte readByte() throws IOException {
            CacheKey cacheKey = cacheKey(fileName, currentPos);
            byte[] cached = cache.get(cacheKey);
            if (cached == null) {
                cached = readBytesForCache();
                cache.put(cacheKey, cached);
            }
            return cached[(int) (currentPos++ % bucketSize)];
        }

        public void readBytes(byte[] b, int offset, int len) throws IOException {
            int indexInCache = (int) (currentPos % bucketSize);
            while (true) {
                CacheKey cacheKey = cacheKey(fileName, currentPos);
                byte[] cached = cache.get(cacheKey);
                if (cached == null) {
                    cached = readBytesForCache();
                    cache.put(cacheKey, cached);
                }
                int sizeToRead = bucketSize - indexInCache;
                if (sizeToRead > len) {
                    sizeToRead = len;
                }
                System.arraycopy(cached, indexInCache, b, offset, sizeToRead);
                offset += sizeToRead;
                len -= sizeToRead;
                currentPos += sizeToRead;
                indexInCache = 0;
                if (len == 0) {
                    break;
                }
            }
        }

        @Override
        public void readBytes(byte[] b, int offset, int len, boolean useBuffer) throws IOException {
            if (!useBuffer) {
                indexInput.seek(currentPos);
                indexInput.readBytes(b, offset, len, useBuffer);
                currentPos += len;
            } else {
                readBytes(b, offset, len);
            }
        }

        public void close() throws IOException {
            indexInput.close();
        }

        public long getFilePointer() {
            return currentPos;
        }

        public void seek(long pos) throws IOException {
            currentPos = pos;
            indexInput.seek(pos);
        }

        public long length() {
            return indexInput.length();
        }

        @Override
        public Object clone() {
            WrappedIndexInput clone = (WrappedIndexInput) super.clone();
            clone.indexInput = (IndexInput) indexInput.clone();
            return clone;
        }

        private byte[] readBytesForCache() throws IOException {
            indexInput.seek(currentPos - (currentPos % bucketSize));
            int size = (int) (length() - currentPos);
            if (size > bucketSize) {
                size = bucketSize;
            }
            byte[] cached = new byte[size];
            indexInput.readBytes(cached, 0, size, false);
            return cached;
        }

        private CacheKey cacheKey(String fileName, long currentPosition) {
            return new CacheKey(fileName, currentPosition - (currentPosition % bucketSize));
        }
    }

    private class CacheKey {
        private final String fileName;

        private final long position;

        private CacheKey(String fileName, long position) {
            this.fileName = fileName;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            CacheKey cacheKey = (CacheKey) o;

            if (position != cacheKey.position) return false;
            if (!fileName.equals(cacheKey.fileName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = fileName.hashCode();
            result = 31 * result + (int) (position ^ (position >>> 32));
            return result;
        }
    }
}
