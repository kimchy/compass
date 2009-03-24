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

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceAnalyzerController;

/**
 * @author kimchy
 */
public class ClassPropertyAnalyzerController extends ClassPropertyMapping implements ResourceAnalyzerController {

    private String nullAnalyzer;

    public Mapping copy() {
        ClassPropertyAnalyzerController analyzerController = new ClassPropertyAnalyzerController();
        super.copy(analyzerController);
        analyzerController.setNullAnalyzer(getNullAnalyzer());
        return analyzerController;
    }

    public String getAnalyzerResourcePropertyName() {
        return mappings.get(getIdPropertyIndex()).getPath().getPath();
    }

    public String getNullAnalyzer() {
        return nullAnalyzer;
    }

    public void setNullAnalyzer(String nullAnalyzer) {
        this.nullAnalyzer = nullAnalyzer;
    }

    public boolean hasNullAnalyzer() {
        return nullAnalyzer != null;
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
