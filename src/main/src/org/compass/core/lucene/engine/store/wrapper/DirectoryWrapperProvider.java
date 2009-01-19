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

import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;

/**
 * A Lucene {@link Directory} wrapper provider, allows to wrap the actual created
 * {@link Directory} with wrappers (for example to provide memory based cache).
 *
 * @author kimchy
 */
public interface DirectoryWrapperProvider {

    /**
     * Wraps the given directory, and returns the wrapped directory instance.
     *
     * @param subIndex The sub index the directory is associated with
     * @param dir      The directory to wrap
     * @return The wrapped directory
     * @throws SearchEngineException
     */
    Directory wrap(String subIndex, Directory dir) throws SearchEngineException;
}
