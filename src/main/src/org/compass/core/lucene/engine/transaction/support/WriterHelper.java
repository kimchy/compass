/*
 * Copyright 2004-2008 the original author or authors.
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

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * Provides helper method to process add/update/delete opeations from a Resource to
 * an IndexWriter. Also provides helper for {@link org.compass.core.lucene.engine.transaction.support.TransactionJob}
 * processing against a writer.
 *
 * @author kimchy
 */
public abstract class WriterHelper {

    /**
     * Adds the given resource to the writer.
     */
    public static void processAdd(IndexWriter writer, InternalResource resource) throws IOException {
        ResourceEnhancer.Result result = ResourceEnhancer.enahanceResource(resource);
        writer.addDocument(result.getDocument(), result.getAnalyzer());
    }

    /**
     * Updates the given resoruce to the writer.
     */
    public static void processUpdate(IndexWriter writer, InternalResource resource) throws IOException {
        ResourceEnhancer.Result result = ResourceEnhancer.enahanceResource(resource);
        writer.updateDocument(new Term(resource.getResourceKey().getUIDPath(), resource.getResourceKey().buildUID()),
                result.getDocument(), result.getAnalyzer());
    }

    /**
     * Deletes the give resoruce key from the writer.
     */
    public static void processDelete(IndexWriter writer, ResourceKey resourceKey) throws IOException {
        writer.deleteDocuments(new Term(resourceKey.getUIDPath(), resourceKey.buildUID()));
    }

    /**
     * Process the transaction job against the writer.
     */
    public static void processJob(IndexWriter writer, TransactionJob job) throws IOException {
        switch (job.getType()) {
            case CREATE:
                processAdd(writer, job.getResource());
                break;
            case UPDATE:
                processUpdate(writer, job.getResource());
                break;
            case DELETE:
                writer.deleteDocuments(job.getUIDTerm());
                break;
        }
    }
}
