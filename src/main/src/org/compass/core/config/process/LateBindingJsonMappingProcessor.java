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

package org.compass.core.config.process;

import java.util.ArrayList;
import java.util.Iterator;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.JsonMapping;
import org.compass.core.mapping.json.JsonMappingIterator;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;

/**
 * @author kimchy
 */
public class LateBindingJsonMappingProcessor implements MappingProcessor {

    private PropertyNamingStrategy namingStrategy;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.namingStrategy = namingStrategy;

        ((InternalCompassMapping) compassMapping).setPath(namingStrategy.getRootPath());
        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof RootJsonObjectMapping) {
                secondPass((RootJsonObjectMapping) aliasMapping, compassMapping);
            }
        }

        return compassMapping;
    }

    private void secondPass(RootJsonObjectMapping rootJsonObjectMapping, CompassMapping fatherMapping) {
        rootJsonObjectMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), rootJsonObjectMapping.getAlias()));
        for (Iterator it = rootJsonObjectMapping.mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            if (mapping instanceof JsonIdMapping) {
                JsonIdMapping jsonIdMapping = (JsonIdMapping) mapping;
                // in case of xml id mapping, we always use it as internal id
                // and build its own internal path (because other xml properties names might be dynamic)
                jsonIdMapping.setInternal(true);
                jsonIdMapping.setPath(namingStrategy.buildPath(rootJsonObjectMapping.getPath(), jsonIdMapping.getName()));
            }
        }
        JsonMappingIterator.iterateMappings(new FullPathCallback(), rootJsonObjectMapping, true);
    }

    private class FullPathCallback implements JsonMappingIterator.JsonMappingCallback {

        private ArrayList<String> pathSteps = new ArrayList<String>();

        private StringBuilder sb = new StringBuilder();

        public void onJsonRootObject(RootJsonObjectMapping jsonObjectMapping) {
            jsonObjectMapping.setFullPath("");
        }

        public void onJsonObject(PlainJsonObjectMapping jsonObjectMapping) {
            addToPath(jsonObjectMapping);
            jsonObjectMapping.setFullPath(currentPath());
            removeFromPath(jsonObjectMapping);
        }

        public void onJsonContent(JsonContentMapping jsonContentMapping) {
            addToPath(jsonContentMapping);
            jsonContentMapping.setFullPath(currentPath());
            removeFromPath(jsonContentMapping);
        }

        public void onJsonProperty(JsonPropertyMapping jsonPropertyMapping) {
            addToPath(jsonPropertyMapping);
            jsonPropertyMapping.setFullPath(currentPath());
            removeFromPath(jsonPropertyMapping);
        }

        public void onJsonArray(JsonArrayMapping jsonArrayMapping) {
            addToPath(jsonArrayMapping);
            jsonArrayMapping.setFullPath(currentPath());
            removeFromPath(jsonArrayMapping);
        }

        public boolean onBeginMultipleMapping(JsonMapping mapping) {
            addToPath(mapping);
            return true;
        }

        public void onEndMultipleMapping(JsonMapping mapping) {
            removeFromPath(mapping);
        }

        private void addToPath(JsonMapping mapping) {
            if (mapping instanceof RootJsonObjectMapping) {
                return;
            }
            String name = mapping.getName();
            if (name == null) {
                throw new IllegalStateException("Internal error in Compass");
            }
            pathSteps.add(name);
        }

        private void removeFromPath(JsonMapping mapping) {
            if (mapping instanceof RootJsonObjectMapping) {
                return;
            }
            if (pathSteps.size() > 0) {
                pathSteps.remove(pathSteps.size() - 1);
            }
        }

        private String currentPath() {
            sb.setLength(0);
            for (int i = 0; i < pathSteps.size(); i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append(pathSteps.get(i));
            }
            return sb.toString();
        }
    }
}