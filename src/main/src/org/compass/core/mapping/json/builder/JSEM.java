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

package org.compass.core.mapping.json.builder;

import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonBoostPropertyMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.JsonPropertyAnalyzerController;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;

/**
 * @author kimchy
 */
public abstract class JSEM {

    private JSEM() {

    }

    public static RootJsonObjectMappingBuilder json(String alias) {
        RootJsonObjectMapping mapping = new RootJsonObjectMapping();
        mapping.setRoot(true);
        mapping.setAlias(alias);
        return new RootJsonObjectMappingBuilder(mapping);
    }

    public static JsonAnalyzerMappingBuilder analyzer(String name) {
        JsonPropertyAnalyzerController mapping = new JsonPropertyAnalyzerController();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        return new JsonAnalyzerMappingBuilder(mapping);
    }

    public static JsonIdMappingBuilder id(String name) {
        JsonIdMapping mapping = new JsonIdMapping();
        mapping.setName(name);
        mapping.setOmitNorms(true);
        mapping.setOmitTf(true);
        return new JsonIdMappingBuilder(mapping);
    }

    /**
     * Used with array.
     */
    public static JsonPropertyMappingBuilder property() {
        return property(null);
    }

    public static JsonPropertyMappingBuilder property(String name) {
        JsonPropertyMapping mapping = new JsonPropertyMapping();
        mapping.setName(name);
        if (name != null) {
            mapping.setPath(new StaticPropertyPath(name));
        }
        return new JsonPropertyMappingBuilder(mapping);
    }

    public static JsonContentMappingBuilder content(String name) {
        JsonContentMapping mapping = new JsonContentMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setInternal(true);
        return new JsonContentMappingBuilder(mapping);
    }

    public static PlainJsonObjectMappingBuilder object() {
        return object(null);
    }

    public static PlainJsonObjectMappingBuilder object(String name) {
        PlainJsonObjectMapping mapping = new PlainJsonObjectMapping();
        mapping.setName(name);
        if (name != null) {
            mapping.setPath(new StaticPropertyPath(name));
        }
        return new PlainJsonObjectMappingBuilder(mapping);
    }

    public static JsonArrayMappingBuilder array(String name) {
        JsonArrayMapping mapping = new JsonArrayMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        return new JsonArrayMappingBuilder(mapping);
    }

    public static JsonBoostMappingBuilder boost(String name) {
        JsonBoostPropertyMapping mapping = new JsonBoostPropertyMapping();
        mapping.setName(name);
        return new JsonBoostMappingBuilder(mapping);
    }

    public static JsonAllMappingBuilder all() {
        DefaultAllMapping allMapping = new DefaultAllMapping();
        return new JsonAllMappingBuilder(allMapping);
    }
}
