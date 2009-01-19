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
import java.util.Arrays;
import java.util.HashSet;
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
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * A local directory cache wraps an actual Lucene directory with a cache Lucene directory.
 * This local cache supports several instnaces working against the same directory.
 *
 * <p>Read operations are performed first by copying the file content to the local cache.
 * list and lock operations are perfomed directly against the actual directory.
 *
 * <p>A scheduled taks runs in a 10 seconds interval and clean up the local cache directory
 * by deleting anything that is in the local cache and not in the remote directory.
 *
 * @author kimchy
 */
public class LocalDirectoryCache extends Directory implements DirectoryWrapper {

    private static final Log log = LogFactory.getLog(LocalDirectoryCache.class);

    private String subIndex;

    private int bufferSize = 16384;

    private Directory dir;

    private Directory localCacheDir;

    private LocalCacheManager localCacheManager;

    private ScheduledFuture cleanupTaskFuture;

    /**
     * Monitors used to control concurrent access to fetch if required
     */
    private Object[] monitors = new Object[100];

    public LocalDirectoryCache(String subIndex, Directory dir, Directory localCacheDir, LocalCacheManager localCacheManager) {
        this(subIndex, dir, localCacheDir, 16384, localCacheManager);
    }

    public LocalDirectoryCache(String subIndex, Directory dir, Directory localCacheDir, int bufferSize, LocalCacheManager localCacheManager) {
        this.subIndex = subIndex;
        this.dir = dir;
        this.localCacheDir = localCacheDir;
        this.bufferSize = bufferSize;
        this.localCacheManager = localCacheManager;

        for (int i = 0; i < monitors.length; i++) {
            monitors[i] = new Object();
        }

        cleanupTaskFuture = localCacheManager.getSearchEngineFactory().getExecutorManager().scheduleWithFixedDelay(new CleanupTask(), 10, 10, TimeUnit.SECONDS);
    }

    public Directory getWrappedDirectory() {
        return this.dir;
    }

    @Override
    public void deleteFile(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            dir.deleteFile(name);
            if (log.isTraceEnabled()) {
                log.trace(logMessage("Deleting [" + name + "] from actual directory"));
            }
            return;
        }
        if (localCacheDir.fileExists(name)) {
            if (log.isTraceEnabled()) {
                log.trace(logMessage("Deleting [" + name + "] from local cache"));
            }
            localCacheDir.deleteFile(name);
        }
        // if this is a compound file extension, don't delete it from the actual directory
        // since we never copied it
        if (localCacheManager.getSearchEngineFactory().getLuceneIndexManager().getStore().isUseCompoundFile() &&
                IndexFileNameFilter.getFilter().isCFSFile(name)) {
            return;
        }
        dir.deleteFile(name);
        if (log.isTraceEnabled()) {
            log.trace(logMessage("Deleting [" + name + "] from actual directory"));
        }
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            return dir.fileExists(name);
        }
        if (localCacheDir.fileExists(name)) {
            return true;
        }
        return dir.fileExists(name);
    }

    @Override
    public long fileLength(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            return dir.fileLength(name);
        }
        fetchFileIfNotExists(name);
        return localCacheDir.fileLength(name);
    }

    @Override
    public long fileModified(String name) throws IOException {
        return dir.fileModified(name);
    }

    @Override
    public String[] list() throws IOException {
        return dir.list();
    }

    @Override
    public void renameFile(String from, String to) throws IOException {
        if (shouldPerformOperationOnActualDirectory(from)) {
            dir.renameFile(from, to);
            return;
        }
        fetchFileIfNotExists(from);
        localCacheDir.renameFile(from, to);
        dir.renameFile(from, to);
    }

    @Override
    public void touchFile(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            dir.touchFile(name);
            return;
        }
        fetchFileIfNotExists(name);
        localCacheDir.touchFile(name);
        dir.touchFile(name);
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
    public void close() throws IOException {
        cleanupTaskFuture.cancel(true);
        localCacheDir.close();
        dir.close();
    }

    @Override
    public IndexInput openInput(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            return dir.openInput(name);
        }
        fetchFileIfNotExists(name);
        return localCacheDir.openInput(name);
    }

    @Override
    public IndexOutput createOutput(String name) throws IOException {
        if (shouldPerformOperationOnActualDirectory(name)) {
            return dir.createOutput(name);
        }
        if (log.isTraceEnabled()) {
            log.trace(logMessage("Creating [" + name + "] in local cache"));
        }
        return new LocalCacheIndexOutput(name, localCacheDir.createOutput(name));
    }

    private boolean shouldPerformOperationOnActualDirectory(String name) {
        return LuceneFileNames.isStaticFile(name);
    }

    private void fetchFileIfNotExists(String name) throws IOException {
        synchronized (monitors[Math.abs(name.hashCode()) % monitors.length]) {
            if (localCacheDir.fileExists(name)) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace(logMessage("Fetching [" + name + "] to local cache"));
            }
            copy(dir, localCacheDir, name);
        }
    }

    private void copy(Directory src, Directory dist, String name) throws IOException {
        byte[] buf = new byte[bufferSize];
        IndexOutput os = null;
        IndexInput is = null;
        try {
            os = dist.createOutput(name);
            is = src.openInput(name);
            long len = is.length();
            long readCount = 0;
            while (readCount < len) {
                int toRead = readCount + bufferSize > len ? (int) (len - readCount) : bufferSize;
                is.readBytes(buf, 0, toRead);
                os.writeBytes(buf, toRead);
                readCount += toRead;
            }
        } finally {
            // graceful cleanup
            try {
                if (os != null)
                    os.close();
            } finally {
                if (is != null)
                    is.close();
            }
        }
    }

    public void clearWrapper() throws IOException {
        if (log.isTraceEnabled()) {
            log.trace(logMessage("Clearing local cache"));
        }
        String[] list = localCacheDir.list();
        for (String name : list) {
            synchronized (monitors[Math.abs(name.hashCode()) % monitors.length]) {
                if (localCacheDir.fileExists(name)) {
                    localCacheDir.deleteFile(name);
                }
            }
        }
    }

    private String logMessage(String message) {
        return "[" + subIndex + "] " + message;
    }


    /**
     * A clean up task that deletes files from the local cache that exist within the local cache
     * and do no exist within the remote directory.
     */
    public class CleanupTask implements Runnable {

        public void run() {
            String[] currentList;
            String[] remoteList;
            try {
                currentList = localCacheDir.list();
                remoteList = localCacheManager.getSearchEngineFactory().getTransactionContext().execute(new TransactionContextCallback<String[]>() {
                    public String[] doInTransaction() throws CompassException {
                        try {
                            return dir.list();
                        } catch (IOException e) {
                            log.error(logMessage("Failed to list directory"), e);
                            return null;
                        }
                    }
                });
                if (remoteList == null) {
                    return;
                }
            } catch (IOException e) {
                log.error(logMessage("Failed to list directory"), e);
                return;
            }

            HashSet<String> filesToCleanUp = new HashSet<String>();
            filesToCleanUp.addAll(Arrays.asList(currentList));

            for (String aRemoteList : remoteList) {
                filesToCleanUp.remove(aRemoteList);
            }

            for (String name : filesToCleanUp) {
                synchronized (monitors[Math.abs(name.hashCode()) % monitors.length]) {
                    try {
                        // don't do anything with a cfs file since it will not be in the actual directory
                        if (localCacheManager.getSearchEngineFactory().getLuceneIndexManager().getStore().isUseCompoundFile() &&
                                IndexFileNameFilter.getFilter().isCFSFile(name)) {
                            continue;
                        }
                        if (localCacheDir.fileExists(name)) {
                            if (log.isTraceEnabled()) {
                                log.trace(logMessage("Clean [" + name + "] from local cache"));
                            }
                            localCacheDir.deleteFile(name);
                        }
                    } catch (IOException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(logMessage("Failed to clean local file [" + name + "]"), e);
                        }
                    }
                }
            }
        }
    }

    public class LocalCacheIndexOutput extends IndexOutput {

        private String name;

        private IndexOutput localCacheIndexOutput;

        public LocalCacheIndexOutput(String name, IndexOutput localCacheIndexOutput) {
            this.name = name;
            this.localCacheIndexOutput = localCacheIndexOutput;
        }

        public void writeByte(byte b) throws IOException {
            localCacheIndexOutput.writeByte(b);
        }

        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            localCacheIndexOutput.writeBytes(b, offset, length);
        }

        public void seek(long size) throws IOException {
            localCacheIndexOutput.seek(size);
        }

        public long length() throws IOException {
            return localCacheIndexOutput.length();
        }

        public long getFilePointer() {
            return localCacheIndexOutput.getFilePointer();
        }

        public void flush() throws IOException {
            localCacheIndexOutput.flush();
        }

        public void close() throws IOException {
            localCacheIndexOutput.close();
            // if we are using compound file extension don't copy them to the actual directory
            // just copy over the cfs file
            if (localCacheManager.getSearchEngineFactory().getLuceneIndexManager().getStore().isUseCompoundFile() &&
                    IndexFileNameFilter.getFilter().isCFSFile(name)) {
                return;
            }
            if (log.isTraceEnabled()) {
                log.trace(logMessage("Creating [" + name + "] in actual directory"));
            }
            copy(localCacheDir, dir, name);
        }
    }
}
