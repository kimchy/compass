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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;
import org.compass.core.json.JsonArray;
import org.compass.core.json.JsonObject;
import org.compass.core.json.impl.DefaultJSONObject;
import org.compass.core.json.impl.DefaultJSONTokener;
import org.compass.core.util.CopyUtils;
import org.compass.core.util.SystemPropertyUtils;

/**
 * A JSON configuration bulider, basically converting the JSON notation to flat settings.
 *
 * @author kimchy
 */
public class JsonConfigurationBuilder extends AbstractInputStreamConfigurationBuilder {

    private ArrayList<String> elements = new ArrayList<String>();

    private StringBuilder sb = new StringBuilder();

    protected void doConfigure(InputStream is, String resourceName, CompassConfiguration config) throws ConfigurationException {
        String json;
        try {
            json = CopyUtils.copyToString(new InputStreamReader(is));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read json configuration from [" + resourceName + "]", e);
        }
        DefaultJSONObject jsonObject = new DefaultJSONObject(new DefaultJSONTokener(json));
        process(config, jsonObject);
    }

    private void process(CompassConfiguration config, Object element) {
        if (element instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) element;
            for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext();) {
                String key = keyIt.next();
                Object value = jsonObject.opt(key);
                if (value == null) {
                    continue;
                }
                elements.add(key);
                process(config, value);
                elements.remove(elements.size() - 1);
            }
        } else if (element instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) element;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.opt(i);
                if (value == null || jsonArray.isNull(i)) {
                    continue;
                }
                process(config, value);
            }
        } else {
            sb.setLength(0);
            int i;
            for (i = 0; i < (elements.size() - 1); i++) {
                sb.append(elements.get(i)).append('.');
            }
            sb.append(elements.get(i));
            config.setSetting(sb.toString(), SystemPropertyUtils.resolvePlaceholders(element.toString()));
        }
    }
}
