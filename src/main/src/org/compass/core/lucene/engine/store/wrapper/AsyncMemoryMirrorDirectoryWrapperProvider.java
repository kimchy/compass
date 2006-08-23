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

package org.compass.core.lucene.engine.store.wrapper;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.wrapper.AsyncMemoryMirrorDirectoryWrapper;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.util.backport.java.util.concurrent.ExecutorService;
import org.compass.core.util.backport.java.util.concurrent.Executors;
import org.compass.core.util.concurrent.SingleThreadThreadFactory;

/**
 * Wraps a Lucene {@link Directory} with {@link AsyncMemoryMirrorDirectoryWrapper}.
 *
 * @author kimchy
 * @see AsyncMemoryMirrorDirectoryWrapper
 */
public class AsyncMemoryMirrorDirectoryWrapperProvider implements DirectoryWrapperProvider, CompassConfigurable {

    private long awaitTermination;

    private boolean sharedThread;

    private ExecutorService executorService;

    /**
     * Configures {@link AsyncMemoryMirrorDirectoryWrapper}.
     * <p/>
     * <code>awaitTermination</code> is the first setting, and defaults to 5 seconds.
     * <code>sharedThread</code> causes all the differnet async wrappers (wrapping
     * the {@link Directory}) to share the same thread (default to <code>true</code>).
     */
    public void configure(CompassSettings settings) throws CompassException {
        awaitTermination = settings.getSettingAsLong("awaitTermination", 5);
        sharedThread = settings.getSettingAsBoolean("sharedThread", true);
        if (sharedThread) {
            executorService = doCreateExecutorService();
        }
    }

    public Directory wrap(String subIndex, Directory dir) throws SearchEngineException {
        try {
            if (sharedThread) {
                return new AsyncMemoryMirrorDirectoryWrapper(dir, awaitTermination, executorService);
            }
            return new AsyncMemoryMirrorDirectoryWrapper(dir, awaitTermination, doCreateExecutorService());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to wrap directory [" + dir + "] with async memory wrapper", e);
        }
    }

    protected ExecutorService doCreateExecutorService() {
        return Executors.newSingleThreadExecutor(new SingleThreadThreadFactory("AsyncMirror Directory Wrapper", false));
    }
}
