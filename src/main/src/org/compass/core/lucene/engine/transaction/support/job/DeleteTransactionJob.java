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
import org.compass.core.lucene.engine.transaction.support.WriterHelper;
import org.compass.core.spi.ResourceKey;

/**
 * A transaction job that deletes a resource based on its {@link org.compass.core.spi.ResourceKey}.
 *
 * @author kimchy
 */
public class DeleteTransactionJob implements TransactionJob {

    private final ResourceKey resourceKey;

    private final String resourceUID;

    public DeleteTransactionJob(ResourceKey resourceKey) {
        this.resourceKey = resourceKey;
        this.resourceUID = resourceKey.buildUID();
    }

    public String getSubIndex() {
        return resourceKey.getSubIndex();
    }

    public String getResourceUID() {
        return resourceUID;
    }

    public ResourceKey getResourceKey() {
        return resourceKey;
    }

    public void execute(IndexWriter writer, LuceneSearchEngineFactory sessionFactory) throws Exception {
        WriterHelper.processDelete(writer, resourceKey);
    }

    @Override
    public String toString() {
        return "Job Delete [" + resourceKey + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteTransactionJob that = (DeleteTransactionJob) o;

        if (resourceKey != null ? !resourceKey.equals(that.resourceKey) : that.resourceKey != null) return false;
        if (resourceUID != null ? !resourceUID.equals(that.resourceUID) : that.resourceUID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 31 * resourceUID.hashCode();
    }
}