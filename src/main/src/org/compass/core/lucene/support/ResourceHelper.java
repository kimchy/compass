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

package org.compass.core.lucene.support;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.spi.ResourceKey;

/**
 * A set of helper methods around {@link org.compass.core.Resource}.
 *
 * @author kimchy
 */
public abstract class ResourceHelper {

    /**
     * Constructs a query that can be used to load the given resource based on the resource key.
     */
    public static Query buildResourceLoadQuery(ResourceKey resourceKey) {
        return new TermQuery(new Term(resourceKey.getUIDPath(), resourceKey.buildUID()));
    }

    /**
     * Converts terms docs into an array of resources.
     */
    public static Resource[] hitsToResourceArray(final TermDocs termDocs, IndexReader indexReader, LuceneSearchEngine searchEngine) throws IOException {
        ArrayList<Resource> list = new ArrayList<Resource>();
        while (termDocs.next()) {
            list.add(new LuceneResource(indexReader.document(termDocs.doc()), termDocs.doc(), searchEngine.getSearchEngineFactory()));
        }
        return list.toArray(new Resource[list.size()]);
    }

    /**
     * Converts a set of hits into an array of resources.
     */
    public static Resource[] hitsToResourceArray(final Hits hits, LuceneSearchEngine searchEngine) throws SearchEngineException {
        int length = hits.length();
        Resource[] result = new Resource[length];
        for (int i = 0; i < length; i++) {
            try {
                result[i] = new LuceneResource(hits.doc(i), hits.id(i), searchEngine.getSearchEngineFactory());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to fetch document from hits.", e);
            }
        }
        return result;
    }
}
