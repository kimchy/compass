/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.lucene.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Lock;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.RepeatableReader;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.MultiIOReader;
import org.compass.core.util.StringReader;

/**
 * @author kimchy
 */
public abstract class LuceneUtils {

    public static Resource[] hitsToResourceArray(final Hits hits, LuceneSearchEngine searchEngine) throws SearchEngineException {
        int length = hits.length();
        Resource[] result = new Resource[length];
        for (int i = 0; i < length; i++) {
            try {
                result[i] = new LuceneResource(hits.doc(i), hits.id(i), searchEngine);
            } catch (IOException e) {
                throw new SearchEngineException("Failed to fetch document from hits.", e);
            }
        }
        return result;
    }

    public static void createResource(IndexWriter indexWriter, Resource resource, Analyzer analyzer) throws SearchEngineException {
        Document document = ((LuceneResource) resource).getDocument();
        try {
            indexWriter.addDocument(document, analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource " + resource, e);
        }
    }

    public static void addAllPropertyIfNeeded(Resource resource, ResourceMapping resourceMapping, LuceneSearchEngine searchEngine) throws SearchEngineException {
        if (resourceMapping.isAllSupported()) {
            LuceneSettings luceneSettings = searchEngine.getSearchEngineFactory().getLuceneSettings();
            PropertyNamingStrategy propertyNamingStrategy = searchEngine.getSearchEngineFactory().getPropertyNamingStrategy();
            MultiIOReader reader = new MultiIOReader();
            Property[] properties = resource.getProperties();
            boolean atleastOneAddedToAll = false;
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                ResourcePropertyMapping resourcePropertyMapping =
                        resourceMapping.getResourcePropertyMappingByPath(property.getName());
                if (resourcePropertyMapping == null) {
                    if (!propertyNamingStrategy.isInternal(property.getName())) {
                        if (resourceMapping.isIncludePropertiesWithNoMappingsInAll()) {
                            atleastOneAddedToAll = tryAddPropertyToAll(property, reader, atleastOneAddedToAll);
                        }
                    }
                } else if (!resourcePropertyMapping.isExcludeFromAll() && !resourcePropertyMapping.isInternal()) {
                    atleastOneAddedToAll = tryAddPropertyToAll(property, reader, atleastOneAddedToAll);
                }
            }
            if (atleastOneAddedToAll) {
                String allP = resourceMapping.getAllProperty();
                if (allP == null) {
                    allP = luceneSettings.getAllProperty();
                }
                Property.TermVector allTermVector = resourceMapping.getAllTermVector();
                if (allTermVector == null) {
                    allTermVector = luceneSettings.getAllPropertyTermVector();
                }
                resource.addProperty(searchEngine.createProperty(allP, reader, allTermVector));
            }
        }
    }

    private static boolean tryAddPropertyToAll(Property property, MultiIOReader reader, boolean atleastOneAddedToAll) {
        String value = property.getStringValue();
        if (value != null) {
            reader.add(new StringReader(value));
            reader.add(new StringReader(" "));
            return true;
        }
        RepeatableReader repeatableReader = ((LuceneProperty) property).getRepeatableReader();
        if (repeatableReader != null) {
            reader.add((Reader) repeatableReader);
            reader.add(new StringReader(" "));
            return true;
        }
        return atleastOneAddedToAll;
    }

    public static List findPropertyValues(IndexReader indexReader, String propertyName) throws SearchEngineException {
        ArrayList list = new ArrayList();
        try {
            TermEnum te = indexReader.terms(new Term(propertyName, ""));
            while (propertyName.equals(te.term().field())) {
                String value = te.term().text();
                list.add(value);
                if (!te.next()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read property values for property [" + propertyName + "]");
        }
        return list;
    }

    public static Field.Index getFieldIndex(Property.Index index) throws SearchEngineException {
        if (index == Property.Index.TOKENIZED) {
            return Field.Index.TOKENIZED;
        }

        if (index == Property.Index.UN_TOKENIZED) {
            return Field.Index.UN_TOKENIZED;
        }

        if (index == Property.Index.NO) {
            return Field.Index.NO;
        }

        throw new SearchEngineException("No index type is defined for [" + index + "]");
    }

    public static Field.Store getFieldStore(Property.Store store) throws SearchEngineException {
        if (store == Property.Store.YES) {
            return Field.Store.YES;
        }

        if (store == Property.Store.NO) {
            return Field.Store.NO;
        }

        if (store == Property.Store.COMPRESS) {
            return Field.Store.COMPRESS;
        }

        throw new SearchEngineException("No store type is defined for [" + store + "]");
    }

    public static Field.TermVector getFieldTermVector(Property.TermVector termVector) throws SearchEngineException {
        if (termVector == Property.TermVector.NO) {
            return Field.TermVector.NO;
        }

        if (termVector == Property.TermVector.YES) {
            return Field.TermVector.YES;
        }

        if (termVector == Property.TermVector.WITH_OFFSETS) {
            return Field.TermVector.WITH_OFFSETS;
        }

        if (termVector == Property.TermVector.WITH_POSITIONS) {
            return Field.TermVector.WITH_POSITIONS;
        }

        if (termVector == Property.TermVector.WITH_POSITIONS_OFFSETS) {
            return Field.TermVector.WITH_POSITIONS_OFFSETS;
        }

        throw new SearchEngineException("No term vector type is defined for [" + termVector + "]");
    }

    public static boolean deleteDir(File dir) {
        boolean globalSuccess = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    globalSuccess = false;
                }
            }
        }
        // The directory is now empty so delete it
        if (!dir.delete()) {
            globalSuccess = false;
        }
        return globalSuccess;
    }

    public static void clearLocks(Lock[] locks) {
        if (locks == null) {
            return;
        }
        for (int i = 0; i < locks.length; i++) {
            if (locks[i] != null) {
                locks[i].release();
            }
        }
    }
}
