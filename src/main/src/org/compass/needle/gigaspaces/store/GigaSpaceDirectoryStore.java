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

package org.compass.needle.gigaspaces.store;

import java.io.IOException;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceFinder;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.store.AbstractDirectoryStore;
import org.compass.core.lucene.engine.store.CopyFromHolder;

/**
 * A plugin lucene store for Compass. Uses {@link GigaSpaceDirectory}
 * as Lucene directory implementation.
 *
 * @author kimchy
 */
public class GigaSpaceDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    public static final String PROTOCOL = "space://";

    public static final String BUCKET_SIZE_PROP = "compass.engine.store.space.bucketSize";

    private String indexName;

    private IJSpace space;

    private int bucketSize;

    public void configure(CompassSettings settings) throws CompassException {
        String connection = settings.getSetting(CompassEnvironment.CONNECTION).substring(PROTOCOL.length());
        int index = connection.indexOf(':');
        this.indexName = connection.substring(0, index);

        String spaceUrl = connection.substring(index + 1);
        bucketSize = settings.getSettingAsInt(BUCKET_SIZE_PROP, GigaSpaceDirectory.DEFAULT_BUCKET_SIZE);
        try {
            space = (IJSpace) SpaceFinder.find(spaceUrl, settings.getProperties());
        } catch (Exception e) {
            throw new ConfigurationException("Failed to find Space [" + spaceUrl + "]", e);
        }
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        return new GigaSpaceDirectory(space, buildFullIndexName(subContext, subIndex), bucketSize);
    }

    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        cleanIndex(dir, subContext, subIndex);
    }

    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        try {
            ((GigaSpaceDirectory) dir).deleteContent();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete index for sub context [" + subContext + "] and sub index [" + subIndex + "]", e);
        }
    }

    public CopyFromHolder beforeCopyFrom(String subContext, Directory[] dirs) throws SearchEngineException {
        for (Directory dir : dirs) {
            try {
                ((GigaSpaceDirectory) dir).deleteContent();
            } catch (IOException e) {
                throw new SearchEngineException("Failed to delete context before copy from", e);
            }
        }
        return new CopyFromHolder();
    }

    private String buildFullIndexName(String subContext, String subIndex) {
        return indexName + "X" + subContext + "X" + subIndex;
    }
}
