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

package org.compass.core.mapping;

/**
 * A property of a {@link org.compass.core.Resource} that controlls the analyzer
 * that will be used in conjuction with the resource. The controller provides the property
 * name, which value will define the analyzer used. Also, a null analyzer can be provided
 * in case the property has no value.
 * 
 * @author kimchy
 */
public interface ResourceAnalyzerController extends Mapping {

    /**
     * Returns the name of the {@link org.compass.core.Resource}
     * {@link org.compass.core.Property} which value will control
     * the analyzer to be used for the resource.
     */
    String getAnalyzerResourcePropertyName();

    /**
     * Returns the name of the analyzer to be used in case the {@link org.compass.core.Resource}
     * {@link org.compass.core.Property} value is <code>null</code>.
     */
    String getNullAnalyzer();

    /**
     * Returns <code>true</code> if the controller has a null analyzer configured.
     */
    boolean hasNullAnalyzer();
    
}
