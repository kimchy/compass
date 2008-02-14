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
import org.apache.lucene.store.DirectoryWrapper;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.AbstractLuceneSearchEngineStore;
import org.compass.core.mapping.CompassMapping;

/**
 * A plugin lucene store for Compass. Uses {@link GigaSpaceDirectory}
 * as Lucene directory implementation.
 *
 * @author kimchy
 */
public class GigaSpaceLuceneSearchEngineStore extends AbstractLuceneSearchEngineStore {

    public static final String BUCKET_SIZE_PROP = "compass.engine.store.space.bucketSize";

    private String indexName;

    private String spaceUrl;

    private IJSpace space;

    private int bucketSize;

    public GigaSpaceLuceneSearchEngineStore(String connection, String subContext) {
        super(connection, subContext);
        int index = connection.indexOf(':');
        this.indexName = connection.substring(0, index) + "X" + subContext;
        this.spaceUrl = connection.substring(index + 1);
    }

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        bucketSize = settings.getSettingAsInt(BUCKET_SIZE_PROP, GigaSpaceDirectory.DEFAULT_BUCKET_SIZE);
        try {
            space = (IJSpace) SpaceFinder.find(spaceUrl, settings.getProperties());
        } catch (Exception e) {
            throw new ConfigurationException("Failed to find Space [" + spaceUrl + "]", e);
        }
        super.configure(searchEngineFactory, settings, mapping);
    }

    protected void doDeleteIndex() throws SearchEngineException {
        String[] subIndexes = getSubIndexes();
        for (String subIndex : subIndexes) {
            template.executeForSubIndex(subIndex, false,
                    new LuceneStoreCallback() {
                        public Object doWithStore(Directory dir) throws IOException {
                            if (dir instanceof DirectoryWrapper) {
                                dir = ((DirectoryWrapper) dir).getWrappedDirectory();
                            }
                            ((GigaSpaceDirectory) dir).deleteContent();
                            return null;
                        }
                    });
        }
    }

    protected Directory doOpenDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        return new GigaSpaceDirectory(space, indexName + "X" + subIndex, bucketSize);
    }

    protected void doCleanIndex(final String subIndex) throws SearchEngineException {
        template.executeForSubIndex(subIndex, new LuceneStoreCallback() {
            public Object doWithStore(Directory dest) {
                if (dest instanceof DirectoryWrapper) {
                    dest = ((DirectoryWrapper) dest).getWrappedDirectory();
                }
                GigaSpaceDirectory gigaDir = (GigaSpaceDirectory) dest;
                try {
                    gigaDir.deleteContent();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to delete content of [" + subIndex + "]", e);
                }
                return null;
            }
        });
    }

    protected CopyFromHolder doBeforeCopyFrom() throws SearchEngineException {
        for (int i = 0; i < getSubIndexes().length; i++) {
            final String subIndex = getSubIndexes()[i];
            template.executeForSubIndex(subIndex, new LuceneStoreCallback() {
                public Object doWithStore(Directory dest) {
                    if (dest instanceof DirectoryWrapper) {
                        dest = ((DirectoryWrapper) dest).getWrappedDirectory();
                    }
                    GigaSpaceDirectory gigaDir = (GigaSpaceDirectory) dest;
                    try {
                        gigaDir.deleteContent();
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to delete content of [" + subIndex + "]", e);
                    }
                    return null;
                }
            });
        }
        CopyFromHolder holder = new CopyFromHolder();
        holder.createOriginalDirectory = false;
        return holder;
    }

}
