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

package org.compass.core.mapping.json;

import org.compass.core.Property;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.support.AbstractMapping;

/**
 * @author kimchy
 */
public class JsonPropertyArrayMapping extends AbstractMapping implements ResourcePropertyMapping, JsonArrayMapping {

    private JsonPropertyMapping elementMapping;

    public Mapping copy() {
        JsonCompoundArrayMapping copy = new JsonCompoundArrayMapping();
        super.copy(copy);
        copy.setElementMapping(getElementMapping().copy());
        return copy;
    }

    public JsonPropertyMapping getElementMapping() {
        return elementMapping;
    }

    public void setElementMapping(JsonPropertyMapping elementMapping) {
        this.elementMapping = elementMapping;
    }

    public String getAnalyzer() {
        return elementMapping.getAnalyzer();
    }

    public String getRootAlias() {
        return elementMapping.getRootAlias();
    }

    public boolean isInternal() {
        return elementMapping.isInternal();
    }

    public float getBoost() {
        return elementMapping.getBoost();
    }

    public Boolean isOmitNorms() {
        return elementMapping.isOmitNorms();
    }

    public ExcludeFromAllType getExcludeFromAll() {
        return elementMapping.getExcludeFromAll();
    }

    public SpellCheckType getSpellCheck() {
        return elementMapping.getSpellCheck();
    }

    public Property.Store getStore() {
        return elementMapping.getStore();
    }

    public Property.Index getIndex() {
        return elementMapping.getIndex();
    }

    public Property.TermVector getTermVector() {
        return elementMapping.getTermVector();
    }

    public ReverseType getReverse() {
        return elementMapping.getReverse();
    }

    public String getNullValue() {
        return elementMapping.getNullValue();
    }

    public boolean hasNullValue() {
        return elementMapping.hasNullValue();
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        return elementMapping.getResourcePropertyConverter();
    }
}