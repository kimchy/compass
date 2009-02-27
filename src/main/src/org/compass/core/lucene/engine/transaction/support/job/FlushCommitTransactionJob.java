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

import org.apache.lucene.index.IndexWriter;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * A flush commit job. Basically, commits the index writer (it can still be used for more operations
 * afterwards).
 *
 * @author kimchy
 */
public class FlushCommitTransactionJob implements TransactionJob {

    private final String subIndex;

    public FlushCommitTransactionJob(String subIndex) {
        this.subIndex = subIndex;
    }

    public String getSubIndex() {
        return subIndex;
    }

    public String getResourceUID() {
        return null;
    }

    public void execute(IndexWriter writer, LuceneSearchEngineFactory sessionFactory) throws Exception {
        writer.commit();
    }

    @Override
    public String toString() {
        return "Job Flush [" + subIndex + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlushCommitTransactionJob that = (FlushCommitTransactionJob) o;
        return subIndex.equals(that.subIndex);
    }

    @Override
    public int hashCode() {
        return 53 * subIndex.hashCode();
    }
}