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

package org.compass.core.config.binding;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.InputStreamMappingResolver;
import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.metadata.CompassMetaData;

/**
 * @author kimchy
 */
public interface MappingBinding {

    void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings);

    boolean addResource(String path) throws ConfigurationException, MappingException;

    boolean addResource(String path, ClassLoader classLoader) throws ConfigurationException, MappingException;

    boolean addURL(URL url) throws ConfigurationException, MappingException;

    boolean addDirectory(File dir) throws ConfigurationException, MappingException;

    boolean addPackage(String packageName) throws ConfigurationException, MappingException;

    boolean addJar(File jar) throws ConfigurationException, MappingException;

    boolean addFile(String filePath) throws ConfigurationException, MappingException;

    boolean addFile(File file) throws ConfigurationException, MappingException;

    boolean addClass(Class clazz) throws ConfigurationException, MappingException;

    boolean addMappingResolver(InputStreamMappingResolver mappingResolver)
            throws ConfigurationException, MappingException;

    boolean addInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException;

    boolean addResourceMapping(ResourceMapping resourceMapping) throws ConfigurationException, MappingException;

    boolean addContractMaping(ContractMapping contractMapping) throws ConfigurationException, MappingException;

    String[] getSuffixes();
}
