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

package org.compass.core.test.engine;

import junit.framework.TestCase;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.DefaultCompassMapping;
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

    public static final String UPDATED_SUFFIX = "updated";

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

        DefaultCompassMapping mapping = new DefaultCompassMapping();
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

    protected Resource getSingleIdResource2(SearchEngine searchEngine) {
        return searchEngine.get(createSingleIdResource2(searchEngine));
    }

    protected void assertSingleIdResource2Exists(SearchEngine searchEngine) {
        Resource r = getSingleIdResource2(searchEngine);
        assertNotNull("single resource id exists", r);
    }

    protected Resource getMulitIdResource(SearchEngine searchEngine) {
        return searchEngine.get(createMultiIdResource(searchEngine));
    }

    protected void assertMulitIdResourceExists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource(searchEngine);
        assertNotNull("multi resource id exists", r);
    }

    protected Resource getMulitIdResource2(SearchEngine searchEngine) {
        return searchEngine.get(createMultiIdResource2(searchEngine));
    }

    protected void assertMulitIdResource2Exists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource2(searchEngine);
        assertNotNull("multi resource 2 id exists", r);
    }

    protected void assertSingleIdResourceNotExists(SearchEngine searchEngine) {
        Resource r = getSingleIdResource(searchEngine);
        assertNull("single resource id don't exists", r);
    }

    protected void assertSingleIdResource2NotExists(SearchEngine searchEngine) {
        Resource r = getSingleIdResource2(searchEngine);
        assertNull("single resource id don't exists", r);
    }

    protected void assertMulitIdResourceNotExists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource(searchEngine);
        assertNull("multi resource id don't exists", r);
    }

    protected void assertMulitIdResource2NotExists(SearchEngine searchEngine) {
        Resource r = getMulitIdResource2(searchEngine);
        assertNull("multi resource id don't exists", r);
    }

    protected void assertSingleIdResourceOriginal(SearchEngine searchEngine) {
        Resource resource = getSingleIdResource(searchEngine);
        assertEquals(VALUE_ID1, resource.getValue(PROPERTY_ID1));
        assertEquals(VALUE_VAL1, resource.getValue(PROPERTY_VAL1));
    }

    protected void assertSingleIdResource2Original(SearchEngine searchEngine) {
        Resource resource = getSingleIdResource(searchEngine);
        assertEquals(VALUE_ID2, resource.getValue(PROPERTY_ID1));
        assertEquals(VALUE_VAL1, resource.getValue(PROPERTY_VAL1));
    }

    protected void assertSingleIdResourceUpdated(SearchEngine searchEngine) {
        Resource resource = getSingleIdResource(searchEngine);
        assertEquals(VALUE_ID1, resource.getValue(PROPERTY_ID1));
        assertEquals(VALUE_VAL1 + UPDATED_SUFFIX, resource.getValue(PROPERTY_VAL1));
    }

    protected void assertSingleId2ResourceUpdated(SearchEngine searchEngine) {
        Resource resource = getSingleIdResource2(searchEngine);
        assertEquals(VALUE_ID2, resource.getValue(PROPERTY_ID1));
        assertEquals(VALUE_VAL1 + UPDATED_SUFFIX, resource.getValue(PROPERTY_VAL1));
    }

    protected void assertMulitIdResourceOriginal(SearchEngine searchEngine) {
        Resource resource = getMulitIdResource(searchEngine);
        assertEquals(VALUE_ID2, resource.getValue(PROPERTY_ID2));
        assertEquals(VALUE_ID3, resource.getValue(PROPERTY_ID3));
        assertEquals(VALUE_VAL2, resource.getValue(PROPERTY_VAL2));
    }

    protected void assertMulitIdResourceUpdated(SearchEngine searchEngine) {
        Resource resource = getMulitIdResource(searchEngine);
        assertEquals(VALUE_ID2, resource.getValue(PROPERTY_ID2));
        assertEquals(VALUE_ID3, resource.getValue(PROPERTY_ID3));
        assertEquals(VALUE_VAL2 + UPDATED_SUFFIX, resource.getValue(PROPERTY_VAL2));
    }

    protected Resource createSingleIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource singleId = resourceFactory.createResource(ALIAS_SINGLE);
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_ID1, VALUE_ID1, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                Property.Index.ANALYZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.ANALYZED));
        ((InternalResource) singleId).addUID();
        return singleId;
    }

    protected Resource createUpdatedSingleIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource resource = createSingleIdResource(searchEngine);
        resource.removeProperties(PROPERTY_VAL1);
        resource.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1 + UPDATED_SUFFIX, Property.Store.YES,
                Property.Index.ANALYZED));
        return resource;
    }

    protected Resource createSingleIdResource2(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource singleId = resourceFactory.createResource(ALIAS_SINGLE);
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_ID1, VALUE_ID1_2, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1, Property.Store.YES,
                Property.Index.ANALYZED));
        singleId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.ANALYZED));
        ((InternalResource) singleId).addUID();
        return singleId;
    }

    protected Resource createUpdatedSingleIdResource2(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource resource = createSingleIdResource2(searchEngine);
        resource.removeProperties(PROPERTY_VAL1);
        resource.addProperty(resourceFactory.createProperty(PROPERTY_VAL1, VALUE_VAL1 + UPDATED_SUFFIX, Property.Store.YES,
                Property.Index.ANALYZED));
        return resource;
    }

    protected Resource createMultiIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource multiId = resourceFactory.createResource(ALIAS_MUTLI);
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID2, VALUE_ID2, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID3, VALUE_ID3, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL2, VALUE_VAL2, Property.Store.YES,
                Property.Index.ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL3, VALUE_VAL3, Property.Store.YES,
                Property.Index.ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.ANALYZED));
        ((InternalResource) multiId).addUID();
        return multiId;
    }

    protected Resource createMultiIdResource2(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource multiId = resourceFactory.createResource(ALIAS_MUTLI);
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID2, VALUE_ID2_2, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_ID3, VALUE_ID3_2, Property.Store.YES,
                Property.Index.NOT_ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL2, VALUE_VAL2, Property.Store.YES,
                Property.Index.ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL3, VALUE_VAL3, Property.Store.YES,
                Property.Index.ANALYZED));
        multiId.addProperty(resourceFactory.createProperty(PROPERTY_VAL4, VALUE_VAL4, Property.Store.YES,
                Property.Index.ANALYZED));
        ((InternalResource) multiId).addUID();
        return multiId;
    }

    protected Resource createUpdatedMultiIdResource(SearchEngine searchEngine) {
        ResourceFactory resourceFactory = searchEngine.getSearchEngineFactory().getResourceFactory();
        Resource resource = createMultiIdResource(searchEngine);
        resource.removeProperties(PROPERTY_VAL2);
        resource.addProperty(resourceFactory.createProperty(PROPERTY_VAL2, VALUE_VAL2 + UPDATED_SUFFIX, Property.Store.YES,
                Property.Index.ANALYZED));
        return resource;
    }

    protected SearchEngineQuery createSinlgeResourceDeleteQuery(SearchEngine searchEngine) {
        return searchEngine.queryBuilder().queryString(PROPERTY_ID1 + ":" + VALUE_ID1).toQuery();
    }

    protected SearchEngineQuery createMultiResourceDeteteQuery(SearchEngine searchEngine) {
        return searchEngine.queryBuilder().queryString("+" + PROPERTY_ID2 + ":" + VALUE_ID2 + " +" + PROPERTY_ID3 + ":" + VALUE_ID3).toQuery();
    }
}
