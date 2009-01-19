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

package org.compass.core.mapping.json;

import org.compass.core.Property;
import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.Mapping;

/**
 * @author kimchy
 */
public class JsonBoostPropertyMapping extends JsonPropertyMapping implements BoostPropertyMapping {

    private float defaultBoost = 1.0f;

    public Mapping copy() {
        JsonBoostPropertyMapping boostPropertyMapping = new JsonBoostPropertyMapping();
        super.copy(boostPropertyMapping);
        boostPropertyMapping.setDefaultBoost(getDefaultBoost());
        return boostPropertyMapping;
    }

    public String getBoostResourcePropertyName() {
        return getPath().getPath();
    }

    public float getDefaultBoost() {
        return defaultBoost;
    }

    public void setDefaultBoost(float defaultBoost) {
        this.defaultBoost = defaultBoost;
    }

    @Override
    public Property.Index getIndex() {
        return Property.Index.NOT_ANALYZED;
    }

    @Override
    public Property.Store getStore() {
        return Property.Store.YES;
    }

    @Override
    public Property.TermVector getTermVector() {
        return Property.TermVector.NO;
    }

    @Override
    public Boolean isOmitNorms() {
        return true;
    }

    @Override
    public Boolean isOmitTf() {
        return true;
    }

    @Override
    public boolean isOverrideByName() {
        return false;
    }
}