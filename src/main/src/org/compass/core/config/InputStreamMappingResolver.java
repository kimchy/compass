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

package org.compass.core.config;

import java.io.InputStream;

import org.compass.core.CompassException;

/**
 * A general interface that can be used to resolve mappings definitions that are
 * not supported by the default add configuration API's.
 * 
 * @author kimchy
 * 
 */
public interface InputStreamMappingResolver {

    /**
     * Returns the name of the mapping builder. For example, for compass-core-mapping it should end
     * with ".cpm.xml".
     */
    String getName();

    /**
     * Returns an <code>InputStream</code> that holds the mapping.
     * 
     * @return InputStream that has the mapping definitions.
     * @throws CompassException
     */
    InputStream getMappingAsInputStream() throws CompassException;
}
