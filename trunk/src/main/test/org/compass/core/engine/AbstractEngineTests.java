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

package org.compass.core.engine;

import junit.framework.TestCase;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalResource;

/**
 * @author kimchy
 */
public abstract class AbstractEngineTests extends TestCase {

    public static final String PROPERTY_ID1 = "id1";

    public static final String PROPERTY_ID2 = "id2";

    public static final String PROPERTY_ID3 = "id3";

    public static final String VALUE_ID1 = "id1value";

    public static final String VALUE_ID2 = "id2value";

    public static final String VALUE_ID3 = "id3value";

    public static final String VALUE_ID1_2 = "id1value_2";

    public static final String VALUE_ID2_2 = "id2value_2";

    public static final String VALUE_ID3_2 = "id3value_2";

    public static final String PROPERTY_VAL1 = "val1";

    public static final String PROPERTY_VAL2 = "val2";

    public static final String PROPERTY_VAL3 = "val3";

    public static final String PROPERTY_VAL4 = "val4";

    public static final String VALUE_VAL1 = "val1value";

    public static final String VALUE_VAL2 = "val2value";

    public static final String VALUE_VAL3 = "val3value";

    public static final String VALUE_VAL4 = "val4value";

    public static final String ALIAS_SINGLE = "singleid";

    public static final String ALIAS_MUTLI = "multiid";

    private CompassSettings settings;

    private CompassMapping mapping;

    public CompassSettings getSettings() {
        return settings;
    }

    public CompassMapping getMapping() {
        return this.mapping;
    }

    protected CompassMapping buildCompassMapping() {
        ResourcePropertyMapping id1 = new MockPropertyMapping(PROPERTY_ID1, new StaticPropertyPath(PROPERTY_ID1));
        ResourcePropertyMapping id2 = new MockPropertyMapping(PROPERTY_ID2, new StaticPropertyPath(PROPERTY_ID2));
        ResourcePropertyMapping id3 = new MockPropertyMapping(PROPERTY_ID3, new StaticPropertyPath(PROPERTY_ID3));

//        PropertyMapping val1 = new MockPropertyMapping(PROPERTY_VAL1, PROPERTY_VAL1);
//        PropertyMapping val2 = new MockPropertyMapping(PROPERTY_VAL2, PROPERTY_VAL2);
//        PropertyMapping val3 = new MockPropertyMapping(PROPERTY_VAL3, PROPERTY_VAL3);
//        PropertyMapping val4 = new MockPropertyMapping(PROPERTY_VAL4, PROPERTY_VAL4);

        ResourceMapping singleIdMapping = new MockResourceMapping(ALIAS_SINGLE);
        ((MockResourceMapping) singleIdMapping).addId(id1);

        ResourceMapping multipleIdMapping = new MockResourceMapping(ALIAS_MUTLI);
        ((MockResourceMapping) multipleIdMapping).addId(id2);
        ((MockResourceMapping) multipleIdMapping).addId(id3);

        CompassMapping mapping = new CompassMapping();
        mapping.addMapping(singleIdMapping);
        mapping.addMapping(multipleIdMapping);

        return mapping;
    }

    protected void setUp() throws Exception {
        super.setUp();
        buildCompassSettings();
        mapping = buildCompassMapping();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected CompassSettings buildCompassSettings() {
        settings = new CompassSettings();
        return settings;
    }

    protected Resource getSingleIdResource(SearchEngine searchEngine) {
        return searchEngine.get(createSingleIdResource(searchEngine));
    }

    protected void assertSingleIdResourceExists(SearchEngine searchEngine) {
        Resource r = getSingleIdResource(searchEngine);
        assertNotNull("single resource id don't exists", r);
    }

    protected Resource getSingleId2Resource(SearchEngine searchEngine) {
        return searchEngine.get(createSingleIdResource2(searchEngine));
    }

    protected void assertSingleId2ResourceExists(SearchEngine searchEngine) {
        Resource r = getSingleId2Resource(searchEngine);
        assertNotNull("single resource id exists", r);
    }

    protected Resource getMulitIdResource(SearchEngine searchEngine) {
        return searchEngine.get(createMultiIdResource(searchEngine));
    }

    protected void assertMulitIdResourceExists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource(searchEngine);
        assertNotNull("multi resource id exists", r);
    }

    protected Resource getMulitId2Resource(SearchEngine searchEngine) {
        return searchEngine.get(createMultiIdResource2(searchEngine));
    }

    protected void assertMulitId2ResourceExists(SearchEngine searchEngine) {
        Resource r = getMulitId2Resource(searchEngine);
        assertNotNull("multi resource 2 id exists", r);
    }

    protected void assertSingleIdResourceNotExists(SearchEngine searchEngine) {
        Resource r = getSingleIdResource(searchEngine);
        assertNull("single resource id don't exists", r);
    }

    protected void assertSingleId2ResourceNotExists(SearchEngine searchEngine) {
        Resource r = getSingleId2Resource(searchEngine);
        assertNull("single resource id don't exists", r);
    }

    protected void assertMulitIdResourceNotExists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource(searchEngine);
        assertNull("multi resource id don't exists", r);
    }

    protected void assertMulitId2ResourceNotExists(SearchEngine searchEngine) {
        Resource r = getMulitId2Resource(searchEngine);
        assertNull("multi resource id don't exists", r);
    }

    protected Resource createSingleIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource singleId = resourceFactory.createResource(ALIAS_SINGLE);
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_ID1, VALUE_ID1, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                Property.Index.TOKENIZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.TOKENIZED));
        ((InternalResource) singleId).addUID();
        return singleId;
    }

    protected Resource createSingleIdResource2(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource singleId = resourceFactory.createResource(ALIAS_SINGLE);
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_ID1, VALUE_ID1_2, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                Property.Index.TOKENIZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.TOKENIZED));
        ((InternalResource) singleId).addUID();
        return singleId;
    }

    protected Resource createMultiIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource multiId = resourceFactory.createResource(ALIAS_MUTLI);
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID2, VALUE_ID2, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID3, VALUE_ID3, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL2, VALUE_VAL2, Property.Store.YES,
                Property.Index.TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL3, VALUE_VAL3, Property.Store.YES,
                Property.Index.TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.TOKENIZED));
        ((InternalResource) multiId).addUID();
        return multiId;
    }

    protected Resource createMultiIdResource2(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource multiId = resourceFactory.createResource(ALIAS_MUTLI);
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID2, VALUE_ID2_2, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID3, VALUE_ID3_2, Property.Store.YES,
                Property.Index.UN_TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL2, VALUE_VAL2, Property.Store.YES,
                Property.Index.TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL3, VALUE_VAL3, Property.Store.YES,
                Property.Index.TOKENIZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.TOKENIZED));
        ((InternalResource) multiId).addUID();
        return multiId;
    }

}
