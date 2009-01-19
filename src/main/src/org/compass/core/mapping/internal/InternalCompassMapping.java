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

package org.compass.core.mapping.internal;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;

/**
 * An internal {@link org.compass.core.mapping.CompassMapping} allowing to
 * perform "destructive" operations. Should not be used by users. Please
 * use {@link org.compass.core.config.CompassConfiguration}.
 *
 * @author kimchy
 */
public interface InternalCompassMapping extends CompassMapping {

    void addMapping(AliasMapping mapping) throws MappingException;

    boolean removeMappingByClass(String className);

    boolean removeMappingByAlias(String alias) throws MappingException;

    void clearMappings();

    void postProcess();

    InternalCompassMapping copy(ConverterLookup converterLookup);

    void setPath(PropertyPath path);
}
