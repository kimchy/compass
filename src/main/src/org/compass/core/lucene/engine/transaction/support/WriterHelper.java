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

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * Provides helper method to process create/update/delete opeations from a Resource to
 * an IndexWriter.
 *
 * @author kimchy
 */
public abstract class WriterHelper {

    /**
     * Adds the given resource to the writer.
     */
    public static void processCreate(IndexWriter writer, InternalResource resource) throws IOException {
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
     * Deletes all the resources that match the given query.
     */
    public static void processDelete(IndexWriter writer, Query query) throws IOException {
        writer.deleteDocuments(query);
    }
}
