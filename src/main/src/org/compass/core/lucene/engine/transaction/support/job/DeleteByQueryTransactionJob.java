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
import org.apache.lucene.search.Query;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.support.WriterHelper;

/**
 * A transaction job that deletes all matching {@link org.apache.lucene.search.Query} hits
 * from the sub index.
 *
 * @author kimchy
 */
public class DeleteByQueryTransactionJob implements TransactionJob {

    private final Query query;

    private final String subIndex;

    public DeleteByQueryTransactionJob(Query query, String subIndex) {
        this.query = query;
        this.subIndex = subIndex;
    }

    public String getSubIndex() {
        return subIndex;
    }

    public String getResourceUID() {
        return null;
    }

    public Query getQuery() {
        return query;
    }

    public void execute(IndexWriter writer, LuceneSearchEngineFactory sessionFactory) throws Exception {
        WriterHelper.processDelete(writer, query);
    }

    @Override
    public String toString() {
        return "Job Delete [" + query + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteByQueryTransactionJob that = (DeleteByQueryTransactionJob) o;
        return subIndex.equals(that.subIndex) && query.equals(that.query);

    }

    @Override
    public int hashCode() {
        return 31 * query.hashCode() + subIndex.hashCode();
    }
}