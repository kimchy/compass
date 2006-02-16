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

package org.compass.core.config.binding;

import org.compass.core.config.ConfigurationException;
import org.compass.core.config.InputStreamMappingResolver;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.metadata.CompassMetaData;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author kimchy
 */
public abstract class AbstractInputStreamMappingBinding implements MappingBinding {

    protected CompassMapping mapping;

    protected CompassMetaData metaData;

    public void setUpBinding(CompassMapping mapping, CompassMetaData metaData) {
        this.mapping = mapping;
        this.metaData = metaData;
    }

    public boolean addResource(String path) throws ConfigurationException, MappingException {
        return addResource(path, Thread.currentThread().getContextClassLoader());
    }

    public boolean addResource(String path, ClassLoader classLoader) throws ConfigurationException, MappingException {
        InputStream rsrc = classLoader.getResourceAsStream(path);
        if (rsrc == null) {
            return false;
        }
        return addInputStream(rsrc, path);
    }

    public boolean addURL(URL url) throws ConfigurationException, MappingException {
        try {
            return addInputStream(url.openStream(), url.toExternalForm());
        } catch (IOException e) {
            throw new ConfigurationException("Failed to open url [" + url.toExternalForm() + "]");
        }
    }

    public boolean addDirectory(File dir) throws ConfigurationException, MappingException {
        boolean addedAtLeastOne = false;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDirectory(files[i]);
            } else if (files[i].getName().endsWith(getSuffix())) {
                boolean retVal = addFile(files[i]);
                if (retVal) {
                    addedAtLeastOne = true;
                }
            }
        }
        return addedAtLeastOne;
    }

    public boolean addJar(File jar) throws ConfigurationException, MappingException {
        final JarFile jarFile;
        try {
            jarFile = new JarFile(jar);
        } catch (IOException ioe) {
            throw new ConfigurationException("Could not configure datastore from jar [" + jar.getName() + "]", ioe);
        }

        boolean addedAtLeastOne = false;
        Enumeration jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) jarEntries.nextElement();
            if (ze.getName().endsWith(getSuffix())) {
                try {
                    boolean retVal = addInputStream(jarFile.getInputStream(ze), ze.getName());
                    if (retVal) {
                        addedAtLeastOne = true;
                    }
                } catch (ConfigurationException me) {
                    throw me;
                } catch (Exception e) {
                    throw new ConfigurationException("Could not configure datastore from jar [" + jar.getAbsolutePath() + "]", e);
                }
            }
        }
        return addedAtLeastOne;
    }

    public boolean addFile(String filePath) throws ConfigurationException, MappingException {
        return addFile(new File(filePath));
    }

    public boolean addFile(File file) throws ConfigurationException, MappingException {
        try {
            return addInputStream(new FileInputStream(file), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not configure mapping from file, file not found ["
                    + file.getAbsolutePath() + "]", e);
        }
    }

    public boolean addClass(Class clazz) throws ConfigurationException, MappingException {
        String fileName = clazz.getName().replace('.', '/') + getSuffix();
        InputStream rsrc = clazz.getClassLoader().getResourceAsStream(fileName);
        if (rsrc == null) {
            return false;
        }
        try {
            return addInputStream(rsrc, fileName);
        } catch (ConfigurationException me) {
            throw new ConfigurationException("Error reading resource [" + fileName + "]", me);
        }
    }

    public boolean addMappingResolver(InputStreamMappingResolver mappingResolver) throws ConfigurationException, MappingException {
        return addInputStream(mappingResolver.getMappingAsInputStream(), mappingResolver.getName());
    }

    public boolean addInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException {
        if (resourceName.indexOf(getSuffix()) == -1) {
            return false;
        }
        try {
            return doAddInputStream(is, resourceName);
        } catch (ConfigurationException e) {
            throw e;
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    protected abstract boolean doAddInputStream(InputStream is, String resourceName)
            throws ConfigurationException, MappingException;

    protected abstract String getSuffix();
}
