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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Lock;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.LuceneResourceFactory;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.all.AllAnalyzer;
import org.compass.core.mapping.AllMapping;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public abstract class LuceneUtils {

    public static Query buildResourceLoadQuery(ResourceKey resourceKey) {
        return new TermQuery(new Term(resourceKey.getUIDPath(), resourceKey.buildUID()));
    }

    public static Resource[] hitsToResourceArray(final TermDocs termDocs, IndexReader indexReader, LuceneSearchEngine searchEngine) throws IOException {
        ArrayList<Resource> list = new ArrayList<Resource>();
        while (termDocs.next()) {
            list.add(new LuceneResource(indexReader.document(termDocs.doc()), termDocs.doc(), searchEngine.getSearchEngineFactory()));
        }
        return list.toArray(new Resource[list.size()]);
    }

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

    public static void applyBoostIfNeeded(InternalResource resource, SearchEngineFactory searchEngineFactory) {
        BoostPropertyMapping boostPropertyMapping = resource.resourceKey().getResourceMapping().getBoostPropertyMapping();
        if (boostPropertyMapping == null) {
            return;
        }
        float boostValue = boostPropertyMapping.getDefaultBoost();
        String boostPropertyName = boostPropertyMapping.getBoostResourcePropertyName();
        String sBoostValue = resource.getValue(boostPropertyName);
        if (!searchEngineFactory.getResourceFactory().isNullValue(sBoostValue)) {
            boostValue = Float.parseFloat(sBoostValue);
        }
        resource.setBoost(boostValue);
    }

    public static void createResource(IndexWriter indexWriter, Resource resource, Analyzer analyzer) throws SearchEngineException {
        Document document = ((LuceneResource) resource).getDocument();
        try {
            indexWriter.addDocument(document, analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource " + resource, e);
        }
    }

    public static void addExtendedProeprty(Resource resource, ResourceMapping resourceMapping, LuceneSearchEngineFactory searchEngineFactory) {
        String extendedAliasProperty = searchEngineFactory.getExtendedAliasProperty();
        resource.removeProperties(extendedAliasProperty);
        for (int i = 0; i < resourceMapping.getExtendedAliases().length; i++) {
            LuceneProperty extendedAliasProp = (LuceneProperty) searchEngineFactory.getResourceFactory().createProperty(extendedAliasProperty,
                    resourceMapping.getExtendedAliases()[i], Property.Store.NO, Property.Index.NOT_ANALYZED);
            extendedAliasProp.getField().setOmitNorms(true);
            resource.addProperty(extendedAliasProp);
        }

    }

    public static Analyzer addAllProperty(InternalResource resource, Analyzer analyzer, ResourceMapping resourceMapping, LuceneSearchEngine searchEngine) throws SearchEngineException {
        AllAnalyzer allAnalyzer = new AllAnalyzer(analyzer, resource, searchEngine);
        AllMapping allMapping = resourceMapping.getAllMapping();
        Property property = ((LuceneResourceFactory) searchEngine.getSearchEngineFactory().getResourceFactory()).createProperty(allMapping.getProperty(), allAnalyzer.createAllTokenStream(), allMapping.getTermVector());
        property.setOmitNorms(allMapping.isOmitNorms());
        property.setOmitTf(allMapping.isOmitTf());
        resource.addProperty(property);
        return allAnalyzer;
    }

    public static List<String> findPropertyValues(IndexReader indexReader, String propertyName) throws SearchEngineException {
        ArrayList<String> list = new ArrayList<String>();
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
        if (index == Property.Index.ANALYZED) {
            return Field.Index.ANALYZED;
        }

        if (index == Property.Index.NOT_ANALYZED) {
            return Field.Index.NOT_ANALYZED;
        }

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
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        globalSuccess = false;
                    }
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
        for (Lock lock : locks) {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
