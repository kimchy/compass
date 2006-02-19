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

package org.compass.core.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.compass.core.config.binding.MappingBinding;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.metadata.CompassMetaData;

/**
 * @author kimchy
 */
public class CompassMappingBinding implements MappingBinding {

    private ArrayList mappingBindings = new ArrayList();

    public void addMappingBinding(MappingBinding mappingBinding) {
        this.mappingBindings.add(mappingBinding);
    }

    public void setUpBinding(CompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            ((MappingBinding) it.next()).setUpBinding(mapping, metaData, settings);
        }
    }

    public boolean addResource(String path) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addResource(path);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource [" + path + "]");
        }
        return hasAddedResource;
    }

    public boolean addResource(String path, ClassLoader classLoader) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addResource(path, classLoader);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match resource [" + path + "] and class loader [" + classLoader + "]");
        }
        return hasAddedResource;
    }

    public boolean addURL(URL url) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addURL(url);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match URL [" + url.toExternalForm() + "]");
        }
        return hasAddedResource;
    }

    public boolean addDirectory(File dir) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addDirectory(dir);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match directory [" + dir.getAbsolutePath() + "]");
        }
        return hasAddedResource;
    }

    public boolean addJar(File jar) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addJar(jar);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match jar [" + jar.getAbsolutePath() + "]");
        }
        return hasAddedResource;
    }

    public boolean addFile(String filePath) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addFile(filePath);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match file [" + filePath + "]");
        }
        return hasAddedResource;
    }

    public boolean addFile(File file) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addFile(file);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match file [" + file.getAbsolutePath() + "]");
        }
        return hasAddedResource;
    }

    public boolean addClass(Class clazz) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addClass(clazz);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match class [" + clazz.getName() + "]");
        }
        return hasAddedResource;
    }

    public boolean addMappingResolver(InputStreamMappingResolver mappingResolver) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addMappingResolver(mappingResolver);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match mapping resolver [" + mappingResolver + "]");
        }
        return hasAddedResource;
    }

    public boolean addInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException {
        boolean hasAddedResource = false;
        for (Iterator it = mappingBindings.iterator(); it.hasNext();) {
            MappingBinding mappingBinding = (MappingBinding) it.next();
            boolean retVal = mappingBinding.addInputStream(is, resourceName);
            if (retVal) {
                hasAddedResource = true;
            }
        }
        if (!hasAddedResource) {
            throw new ConfigurationException("No mapping match input stream [" +resourceName + "]");
        }
        return hasAddedResource;
    }
}
