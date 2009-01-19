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

package org.compass.core.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.compass.core.config.ConfigurationException;
import org.compass.core.json.JsonArray;
import org.compass.core.json.JsonObject;
import org.compass.core.json.impl.DefaultJSONObject;
import org.compass.core.json.impl.DefaultJSONTokener;
import org.compass.core.util.CopyUtils;

/**
 * A JSON configuration helper parser.
 *
 * @author kimchy
 */
public class JsonConfigurationHelperBuilder {

    /**
     * Build a configuration object from a file using a filename.
     */
    public ConfigurationHelper buildFromFile(final String filename) throws ConfigurationException {
        return buildFromFile(new File(filename));
    }

    /**
     * Build a configuration object from a file using a File object.
     */
    public ConfigurationHelper buildFromFile(final File file) throws ConfigurationException {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Failed to find file [" + file.getAbsolutePath() + "]");
        }
        return build(inputStream, file.getAbsolutePath());
    }

    /**
     * Build a configuration object using an InputStream; supplying a systemId
     * to make messages about all kinds of errors more meaningfull.
     */
    public ConfigurationHelper build(final InputStream inputStream, final String systemId)
            throws ConfigurationException {
        return build(new InputStreamReader(inputStream), systemId);
    }

    public ConfigurationHelper build(final Reader reader, final String systemId) {
        JsonObject jsonObject;
        try {
            String json = CopyUtils.copyToString(reader);
            jsonObject = new DefaultJSONObject(new DefaultJSONTokener(json));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse JSON document [" + systemId + "]", e);
        }
        PlainConfigurationHelper confHelper = new PlainConfigurationHelper("root", systemId);
        process(confHelper, jsonObject);
        if (confHelper.getChildren().length == 1) {
            return confHelper.getChildren()[0];
        }
        return confHelper;
    }

    private void process(PlainConfigurationHelper confHelper, JsonObject jsonObject) {
        for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext();) {
            String key = keyIt.next();
            Object value = jsonObject.opt(key);
            if (value == null) {
                continue;
            }
            if (value instanceof JsonObject) {
                PlainConfigurationHelper childConfHelper = new PlainConfigurationHelper(key);
                process(childConfHelper, (JsonObject) value);
                confHelper.addChild(childConfHelper);
            } else if (value instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    value = jsonArray.opt(i);
                    if (value == null || jsonArray.isNull(i)) {
                        continue;
                    }
                    if (value instanceof JsonObject) {
                        PlainConfigurationHelper childConfHelper = new PlainConfigurationHelper(key);
                        process(childConfHelper, (JsonObject) value);
                        confHelper.addChild(childConfHelper);
                    } else if (value instanceof JsonArray) {
                        throw new ConfigurationException("Parsing of JSON does not support array within an array");
                    } else {
                        PlainConfigurationHelper childConfHelper = new PlainConfigurationHelper(key);
                        childConfHelper.setValue(value.toString());
                        confHelper.addChild(childConfHelper);
                    }
                }
            } else {
                confHelper.setAttribute(key, value.toString());
            }
        }
    }
}
