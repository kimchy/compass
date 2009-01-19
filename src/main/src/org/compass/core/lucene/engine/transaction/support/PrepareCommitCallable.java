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

package org.compass.core.lucene.engine.transaction.support;

import java.util.concurrent.Callable;

import org.apache.lucene.index.IndexWriter;
import org.compass.core.engine.SearchEngineException;

/**
 * A simple callable that calls {@link org.apache.lucene.index.IndexWriter#prepareCommit()}. Nothing
 * is done in case of an exception, it is propagated outside of the callable.
 *
 * @author kimchy
 */
public class PrepareCommitCallable implements Callable {

    private final String subIndex;

    private final IndexWriter indexWriter;

    public PrepareCommitCallable(String subIndex, IndexWriter indexWriter) {
        this.subIndex = subIndex;
        this.indexWriter = indexWriter;
    }

    public Object call() throws Exception {
        try {
            indexWriter.prepareCommit();
        } catch (Exception e) {
            throw new SearchEngineException("Failed to call prepare commit on sub index [" + subIndex + "]", e);
        }
        return null;
    }
}
