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

package org.compass.core.config.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.ConfigurationException;

/**
 * @author kimchy
 */
public class SmartConfigurationBuilder implements ConfigurationBuilder {

    private static final Log log = LogFactory.getLog(SmartConfigurationBuilder.class);

    /**
     * The maximum number of lines the validation autodetection process should peek into
     * a file looking for the <code>DOCTYPE</code> definition or <code>{</code> Json.
     */
    private static final int MAX_PEEK_LINES = 5;

    public void configure(String resource, CompassConfiguration config) throws ConfigurationException {
        InputStream stream = CompassEnvironment.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new ConfigurationException("Failed to open config resource [" + resource + "]");
        }
        detect(stream, resource).configure(resource, config);
    }

    public void configure(URL url, CompassConfiguration config) throws ConfigurationException {
        InputStream stream;
        try {
            stream = url.openStream();
        } catch (IOException e) {
            throw new ConfigurationException("Failed to open url [" + url.toExternalForm() + "]", e);
        }
        detect(stream, url.toExternalForm()).configure(url, config);
    }

    public void configure(File file, CompassConfiguration config) throws ConfigurationException {
        InputStream stream;
        try {
            stream = new FileInputStream(file);
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Could not find configuration file [" + file.getAbsolutePath() + "]", e);
        }
        detect(stream, file.getAbsolutePath()).configure(file, config);
    }

    private ConfigurationBuilder detect(InputStream stream, String resourceName) {
        if (resourceName.endsWith(".properties")) {
            return new PropertiesConfigurationBuilder();
        }
        if (resourceName.endsWith(".json")) {
            return new JsonConfigurationBuilder();
        }
        //peek into the file to look for DOCTYPE
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            boolean isDtdValidated = false;

            for (int x = 0; x < MAX_PEEK_LINES; x++) {
                String line = reader.readLine();
                if (line == null) {
                    // end of stream
                    break;
                } else if (line.trim().startsWith("{")) {
                    return new JsonConfigurationBuilder();
                } else if (line.indexOf("DOCTYPE") > -1) {
                    return new DTDConfigurationBuilder();
                }
            }
            return new SchemaConfigurationBuilder();
        }
        catch (IOException ex) {
            throw new ConfigurationException(
                    "Unable to determine validation mode for [" + resourceName +
                            "]. Did you attempt to load directly from a SAX InputSource?", ex);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to close BufferedReader for [" + resourceName + "].", ex);
                    }
                }
            }
        }
    }

}
