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

package org.compass.annotations.config;

import org.compass.annotations.config.binding.AnnotationsMappingBinding;
import org.compass.annotations.config.binding.OverrideAnnotationsWithCpmMappingBinding;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassMappingBinding;

/**
 * @author kimchy
 */
public class CompassAnnotationsConfiguration extends CompassConfiguration {

    protected void addMappingBindings(CompassMappingBinding mappingBinding) {
        super.addMappingBindings(mappingBinding);
        mappingBinding.addMappingBinding(new AnnotationsMappingBinding());
        mappingBinding.addMappingBinding(new OverrideAnnotationsWithCpmMappingBinding());
    }
}
