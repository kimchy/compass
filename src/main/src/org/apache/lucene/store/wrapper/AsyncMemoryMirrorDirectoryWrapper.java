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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.RAMOutputStream;
import org.compass.core.util.concurrent.NamedThreadFactory;

/**
 * Wraps a Lucene {@link org.apache.lucene.store.Directory} with
 * an in memory directory which mirrors it asynchronously.
 * <p/>
 * The original directory is read into memory when this wrapper
 * is constructed. All read realted operations are performed
 * against the in memory directory. All write related operations
 * are performed against the in memeory directory and are scheduled
 * to be performed against the original directory (using {@link ExecutorService}).
 * Locking is performed using the in memory directory.
 * <p/>
 * NOTE: This wrapper will only work in cases when either the
 * index is read only (i.e. only search operations are performed
 * against it), or when there is a single instance which updates
 * the directory.
 *
 * @author kimchy
 */
public class AsyncMemoryMirrorDirectoryWrapper extends Directory {

    private static final Log log = LogFactory.getLog(AsyncMemoryMirrorDirectoryWrapper.class);

    private Directory dir;

    private RAMDirectory ramDir;

    private ExecutorService executorService;

    private long awaitTermination;

    public AsyncMemoryMirrorDirectoryWrapper(Directory dir) throws IOException {
        this(dir, 2);
    }

    public AsyncMemoryMirrorDirectoryWrapper(Directory dir, long awaitTermination) throws IOException {
        this(dir, awaitTermination, Executors.newSingleThreadExecutor(new NamedThreadFactory("AsyncMirror[" + dir + "]", false)));
    }

    public AsyncMemoryMirrorDirectoryWrapper(Directory dir, long awaitTermination, ExecutorService executorService) throws IOException {
        this.dir = dir;
        this.ramDir = new RAMDirectory(dir);
        this.executorService = executorService;
        this.awaitTermination = awaitTermination;
    }

    public void deleteFile(final String name) throws IOException {
        ramDir.deleteFile(name);
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    dir.deleteFile(name);
                } catch (IOException e) {
                    logAsyncErrorMessage("delete [" + name + "]");
                }
            }
        });
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

    public void renameFile(final String from, final String to) throws IOException {
        ramDir.renameFile(from, to);
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    dir.renameFile(from, to);
                } catch (IOException e) {
                    logAsyncErrorMessage("rename from[" + from + "] to[" + to + "]");
                }
            }
        });
    }

    public void touchFile(final String name) throws IOException {
        ramDir.touchFile(name);
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    dir.touchFile(name);
                } catch (IOException e) {
                    logAsyncErrorMessage("touch [" + name + "]");
                }
            }
        });
    }

    public Lock makeLock(String name) {
        return ramDir.makeLock(name);
    }

    public void close() throws IOException {
        ramDir.close();
        if (log.isDebugEnabled()) {
            log.debug("Directory [" + dir + "] shutsdown, waiting for [" + awaitTermination +
                    "] minutes for tasks to finish executing");
        }
        executorService.shutdown();
        if (!executorService.isTerminated()) {
            try {
                if (!executorService.awaitTermination(60 * awaitTermination, TimeUnit.SECONDS)) {
                    logAsyncErrorMessage("wait for async tasks to shutdown");
                }
            } catch (InterruptedException e) {
                logAsyncErrorMessage("wait for async tasks to shutdown");
            }
        }
        dir.close();
    }

    public IndexInput openInput(String name) throws IOException {
        return ramDir.openInput(name);
    }

    public IndexOutput createOutput(String name) throws IOException {
        return new AsyncMemoryMirrorIndexOutput(name, (RAMOutputStream) ramDir.createOutput(name));
    }

    private void logAsyncErrorMessage(String message) {
        log.error("Async wrapper for [" + dir + "] failed to " + message);
    }

    public class AsyncMemoryMirrorIndexOutput extends IndexOutput {

        private String name;

        private RAMOutputStream ramIndexOutput;

        public AsyncMemoryMirrorIndexOutput(String name, RAMOutputStream ramIndexOutput) {
            this.name = name;
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
            executorService.submit(new Runnable() {
                public void run() {
                    try {
                        IndexOutput indexOutput = dir.createOutput(name);
                        ramIndexOutput.writeTo(indexOutput);
                        indexOutput.close();
                    } catch (IOException e) {
                        logAsyncErrorMessage("write [" + name + "]");
                    }
                }
            });
        }
    }
}
