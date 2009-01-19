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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.compass.core.config.binding.MappingBinding;
import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.metadata.CompassMetaData;

/**
 * Compass {@link org.compass.core.config.binding.MappingBinding} is a mapping binding that holds
 * a list of {@link org.compass.core.config.binding.MappingBinding}s (ordered) and when trying to
 * add mappings, it will iterate through them and try and add it to each one.
 *
 * @author kimchy
 */
public class CompassMappingBinding implements MappingBinding {

    private List<MappingBinding> mappingBindings = new ArrayList<MappingBinding>();

    private InternalCompassMapping mapping;

    private String[] suffixes;

    public void addMappingBinding(MappingBinding mappingBinding) {
        this.mappingBindings.add(mappingBinding);
    }

    public void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        this.mapping = mapping;
        for (MappingBinding mappingBinding : mappingBindings) {
            (mappingBinding).setUpBinding(mapping, metaData, settings);
        }
        Set<String> suffixes = new HashSet<String>();
        for (MappingBinding mappingBinding : mappingBindings) {
            if (mappingBinding.getSuffixes() != null) {
                suffixes.addAll(Arrays.asList(mappingBinding.getSuffixes()));
            }
        }
        this.suffixes = suffixes.toArray(new String[suffixes.size()]);
    }

    public boolean addResourceMapping(ResourceMapping resourceMapping) throws ConfigurationException, MappingException {
        mapping.addMapping(resourceMapping);
        return true;
    }

    public boolean addContractMaping(ContractMapping contractMapping) throws ConfigurationException, MappingException {
        mapping.addMapping(contractMapping);
        return true;
    }

    public boolean addResource(String path) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addResource(path);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addResource(String path, ClassLoader classLoader) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addResource(path, classLoader);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addURL(URL url) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addURL(url);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addDirectory(File dir) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addDirectory(dir);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addJar(File jar) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addJar(jar);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addFile(String filePath) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addFile(filePath);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addFile(File file) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addFile(file);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addPackage(String packageName) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addPackage(packageName);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addClass(Class clazz) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addClass(clazz);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addMappingResolver(InputStreamMappingResolver mappingResolver) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addMappingResolver(mappingResolver);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public boolean addInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (MappingBinding mappingBinding : mappingBindings) {
            boolean retVal = mappingBinding.addInputStream(is, resourceName);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        return hasAddedResource;
    }

    public String[] getSuffixes() {
        return this.suffixes;
    }
}
