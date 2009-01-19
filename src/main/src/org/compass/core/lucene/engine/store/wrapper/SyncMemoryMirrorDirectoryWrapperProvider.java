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

package org.compass.core.lucene.engine.store.wrapper;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.wrapper.SyncMemoryMirrorDirectoryWrapper;
import org.compass.core.engine.SearchEngineException;

/**
 * Wraps a Lucene {@link Directory} with {@link SyncMemoryMirrorDirectoryWrapper}.
 *
 * @author kimchy
 * @see SyncMemoryMirrorDirectoryWrapper
 */
public class SyncMemoryMirrorDirectoryWrapperProvider implements DirectoryWrapperProvider {

    public Directory wrap(String subIndex, Directory dir) throws SearchEngineException {
        try {
            return new SyncMemoryMirrorDirectoryWrapper(dir);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to wrap directory [" + dir + "] with sync memory wrapper", e);
        }
    }
}
