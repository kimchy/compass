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
import org.compass.core.spi.InternalResource;

/**
 * A transaction job that creates a new {@link org.compass.core.Resource}.
 *
 * @author kimchy
 */
public class CreateTransactionJob implements TransactionJob {

    private final InternalResource resource;

    private final String resourceUID;

    public CreateTransactionJob(InternalResource resource) {
        this.resource = resource;
        this.resourceUID = resource.getResourceKey().buildUID();
    }

    public String getSubIndex() {
        return resource.getSubIndex();
    }

    public String getResourceUID() {
        return resourceUID;
    }

    public InternalResource getResource() {
        return resource;
    }

    public void execute(IndexWriter writer, LuceneSearchEngineFactory sessionFactory) throws Exception {
        resource.attach(sessionFactory);
        WriterHelper.processCreate(writer, resource);
    }

    @Override
    public String toString() {
        return "Job Create [" + resource.getResourceKey() + "] Resource [" + resource + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreateTransactionJob that = (CreateTransactionJob) o;
        return (resourceUID.equals(that.resourceUID));
    }

    @Override
    public int hashCode() {
        return 37 * resourceUID.hashCode();
    }
}