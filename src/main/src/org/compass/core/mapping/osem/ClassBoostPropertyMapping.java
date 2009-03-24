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

package org.compass.core.mapping.osem;

import org.compass.core.mapping.BoostPropertyMapping;
import org.compass.core.mapping.Mapping;

/**
 * @author kimchy
 */
public class ClassBoostPropertyMapping extends ClassPropertyMapping implements BoostPropertyMapping {

    private float defaultBoost = 1.0f;

    public Mapping copy() {
        ClassBoostPropertyMapping boostResourcePropertyMapping = new ClassBoostPropertyMapping();
        super.copy(boostResourcePropertyMapping);
        boostResourcePropertyMapping.setDefaultBoost(getDefaultBoost());
        return boostResourcePropertyMapping;
    }

    public String getBoostResourcePropertyName() {
        return mappings.get(getIdPropertyIndex()).getPath().getPath();
    }

    public float getDefaultBoost() {
        return defaultBoost;
    }

    public void setDefaultBoost(float defaultBoost) {
        this.defaultBoost = defaultBoost;
    }

    @Override
    public boolean isOverrideByName() {
        return false;
    }

    @Override
    public boolean requiresIdProcessing() {
        return true;
    }
}