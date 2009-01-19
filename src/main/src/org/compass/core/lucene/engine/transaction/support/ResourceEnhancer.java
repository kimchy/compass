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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.compass.core.Property;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.all.AllAnalyzer;
import org.compass.core.lucene.support.FieldHelper;
import org.compass.core.mapping.AllMapping;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalResource;

/**
 * A helper class providing helper method to enhance a resource before indexing it.
 *
 * @author kimchy
 */
public class ResourceEnhancer {


    /**
     * Result of {@link org.compass.core.lucene.engine.transaction.support.ResourceEnhancer#enahanceResource(org.compass.core.spi.InternalResource)}.
     * Provides the Lucene {@link org.apache.lucene.document.Document} and {@link org.apache.lucene.analysis.Analyzer} to
     * be used with create/update IndexWriter operations.
     */
    public static class Result {
        private final Analyzer analyzer;

        private final Document document;

        public Result(Analyzer analyzer, Document document) {
            this.analyzer = analyzer;
            this.document = document;
        }

        public Analyzer getAnalyzer() {
            return analyzer;
        }

        public Document getDocument() {
            return document;
        }
    }

    /**
     * Enhances the given resource and reutrns the {@link org.apache.lucene.analysis.Analyzer} that should be
     * used when adding the Lucene Resource to the index.
     *
     * @param resource            The resource to enhance.
     * @return The analyzer that should be used when adding the Lucene resource to the search engine
     */
    public static Result enahanceResource(InternalResource resource) {
        LuceneResource luceneResource = (LuceneResource) resource;
        LuceneSearchEngineFactory searchEngineFactory = luceneResource.getSearchEngineFactory();
        applyBoostIfNeeded(resource, searchEngineFactory);
        addExtendedProeprty(resource, searchEngineFactory);
        Analyzer analyzer = searchEngineFactory.getAnalyzerManager().getAnalyzerByResource(resource);
        Document document = luceneResource.getDocument();
        analyzer = addAllProperty(document, resource, analyzer, searchEngineFactory);
        return new Result(analyzer, document);
    }

    private static void applyBoostIfNeeded(InternalResource resource, SearchEngineFactory searchEngineFactory) {
        BoostPropertyMapping boostPropertyMapping = resource.getResourceMapping().getBoostPropertyMapping();
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

    private static void addExtendedProeprty(InternalResource resource, LuceneSearchEngineFactory searchEngineFactory) {
        String extendedAliasProperty = searchEngineFactory.getExtendedAliasProperty();
        resource.removeProperties(extendedAliasProperty);
        ResourceMapping resourceMapping = resource.getResourceMapping();
        for (int i = 0; i < resourceMapping.getExtendedAliases().length; i++) {
            LuceneProperty extendedAliasProp = (LuceneProperty) searchEngineFactory.getResourceFactory().createProperty(extendedAliasProperty,
                    resourceMapping.getExtendedAliases()[i], Property.Store.NO, Property.Index.NOT_ANALYZED);
            extendedAliasProp.getField().setOmitNorms(true);
            extendedAliasProp.getField().setOmitTf(true);
            resource.addProperty(extendedAliasProp);
        }

    }

    private static Analyzer addAllProperty(Document document, InternalResource resource, Analyzer analyzer, LuceneSearchEngineFactory searchEngineFactory) throws SearchEngineException {
        AllAnalyzer allAnalyzer = new AllAnalyzer(analyzer, resource, searchEngineFactory);
        AllMapping allMapping = resource.getResourceMapping().getAllMapping();
        Fieldable allField = new Field(allMapping.getProperty(), allAnalyzer.createAllTokenStream(), FieldHelper.getFieldTermVector(allMapping.getTermVector()));
        allField.setOmitNorms(allMapping.isOmitNorms());
        allField.setOmitTf(allMapping.isOmitTf());
        document.add(allField);
        return allAnalyzer;
    }

}
