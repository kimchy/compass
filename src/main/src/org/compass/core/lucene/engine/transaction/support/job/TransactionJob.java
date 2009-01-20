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

package org.compass.core.lucene.engine.transaction.support.job;

import java.io.Serializable;

import org.apache.lucene.index.IndexWriter;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * A transaction job represents a job that needs to be performed.
 *
 * @author kimchy
 */
public interface TransactionJob extends Serializable {

    /**
     * Returns the sub index the transaction job will work against.
     */
    String getSubIndex();

    /**
     * Retruns the resource UID (if available) that transaction job will work on.
     */
    String getResourceUID();

    /**
     * Executes the job against Lucene {@link org.apache.lucene.index.IndexWriter}.
     */
    void execute(IndexWriter writer, LuceneSearchEngineFactory sessionFactory) throws Exception;
}
