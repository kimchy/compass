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
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Lock;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.all.AllAnalyzer;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public abstract class LuceneUtils {

    public static Query buildResourceLoadQuery(LuceneSearchEngineFactory searchEngineFactory, ResourceKey resourceKey) {
        return buildResourceLoadQuery(searchEngineFactory, ResourceHelper.computeSubIndex(resourceKey), resourceKey);
    }

    public static Query buildResourceLoadQuery(LuceneSearchEngineFactory searchEngineFactory, String subIndex, ResourceKey resourceKey) {
        Property[] ids = resourceKey.getIds();
        int numberOfAliases = searchEngineFactory.getLuceneIndexManager().getStore().getNumberOfAliasesBySubIndex(subIndex);
        Query query;
        if (numberOfAliases == 1 && ids.length == 1) {
            query = new TermQuery(new Term(ids[0].getName(), ids[0].getStringValue()));
        } else {
            BooleanQuery bQuery = new BooleanQuery();
            if (numberOfAliases > 1) {
                String aliasProperty = searchEngineFactory.getLuceneSettings().getAliasProperty();
                Term t = new Term(aliasProperty, resourceKey.getAlias());
                bQuery.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
            for (int i = 0; i < ids.length; i++) {
                Term t = new Term(ids[i].getName(), ids[i].getStringValue());
                bQuery.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
            query = bQuery;
        }
        return query;
    }

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

    public static void applyBoostIfNeeded(InternalResource resource, SearchEngine searchEngine) {
        BoostPropertyMapping boostPropertyMapping = resource.resourceKey().getResourceMapping().getBoostPropertyMapping();
        if (boostPropertyMapping == null) {
            return;
        }
        float boostValue = boostPropertyMapping.getDefaultBoost();
        String boostPropertyName = boostPropertyMapping.getBoostResourcePropertyName();
        String sBoostValue = resource.getValue(boostPropertyName);
        if (!searchEngine.isNullValue(sBoostValue)) {
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

    public static void addExtendedProeprty(Resource resource, ResourceMapping resourceMapping, LuceneSearchEngine searchEngine) {
        String extendedAliasProperty = searchEngine.getSearchEngineFactory().getExtendedAliasProperty();
        resource.removeProperties(extendedAliasProperty);
        for (int i = 0; i < resourceMapping.getExtendedAliases().length; i++) {
            LuceneProperty extendedAliasProp = (LuceneProperty) searchEngine.createProperty(extendedAliasProperty,
                    resourceMapping.getExtendedAliases()[i], Property.Store.NO, Property.Index.UN_TOKENIZED);
            extendedAliasProp.getField().setOmitNorms(true);
            resource.addProperty(extendedAliasProp);
        }

    }

    public static Analyzer addAllProperty(InternalResource resource, Analyzer analyzer, ResourceMapping resourceMapping, LuceneSearchEngine searchEngine) throws SearchEngineException {
        AllAnalyzer allAnalyzer = new AllAnalyzer(analyzer, resource, searchEngine);
        LuceneSettings luceneSettings = searchEngine.getSearchEngineFactory().getLuceneSettings();
        String allP = resourceMapping.getAllProperty();
        if (allP == null) {
            allP = luceneSettings.getAllProperty();
        }
        Property.TermVector allTermVector = resourceMapping.getAllTermVector();
        if (allTermVector == null) {
            allTermVector = luceneSettings.getAllPropertyTermVector();
        }

        Property property = searchEngine.createProperty(allP, allAnalyzer.createAllTokenStream(), allTermVector);
        property.setOmitNorms(resourceMapping.isAllOmitNorms());
        resource.addProperty(property);
        return allAnalyzer;
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
