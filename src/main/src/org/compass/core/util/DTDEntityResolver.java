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

package org.compass.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.compass.core.config.ConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class DTDEntityResolver implements EntityResolver, Serializable {

    private static final long serialVersionUID = 3256440291954406962L;

    private static final String URL = "http://www.compass-project.org/dtd/";

    private transient ClassLoader resourceLoader;

    /**
     * Default constructor using DTDEntityResolver classloader for resource
     * loading.
     */
    public DTDEntityResolver() {
    }

    /**
     * Set the class loader used to load resouces
     *
     * @param resourceLoader class loader to use
     */
    public DTDEntityResolver(ClassLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId != null && systemId.startsWith("http://compass.sourceforge.net")) {
            throw new IllegalArgumentException("Using old format for DTD, please use the url [" + URL + "]");
        }
        if (systemId != null && systemId.startsWith("http://static.compassframework")) {
            throw new IllegalArgumentException("Using old format for DTD, please use the url [" + URL + "]");
        }
        if (systemId != null && systemId.startsWith("http://www.opensymphony.com/compass/dtd/")) {
            throw new IllegalArgumentException("Using old format for DTD, please use the url [" + URL + "]");
        }
        if (systemId != null && systemId.startsWith(URL)) {
            // Search for DTD
            String location = "/org/compass/core/" + systemId.substring(URL.length());
            InputStream dtdStream = resourceLoader == null ?
                    getClass().getResourceAsStream(location) : resourceLoader.getResourceAsStream(location);
            if (dtdStream == null) {
                throw new ConfigurationException("DTD system id [" + systemId + "] not found at [" + location + "], " +
                        "please check it has the correct location. Have you included compass in your class path?");
            } else {
                InputSource source = new InputSource(dtdStream);
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            }
        } else {
            throw new ConfigurationException("DTD system id [" + systemId + "] not found, please check it has the " +
                    "correct location");
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        /** to allow serialization of configuration */
        ois.defaultReadObject();
        this.resourceLoader = this.getClass().getClassLoader();
    }
}
