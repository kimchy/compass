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

package org.compass.core.lucene.engine.store;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.mapping.CompassMapping;

public class FSLuceneSearchEngineStore extends AbstractLuceneSearchEngineStore {

    private String indexPath;

    public FSLuceneSearchEngineStore(String indexPath, String subContext) {
        this.indexPath = indexPath + "/" + subContext;
    }
    
    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        System.setProperty("org.apache.lucene.FSDirectory.class", getFSDirectoryClass());
        FSDirectory directory;
        try {
            directory = FSDirectory.getDirectory(System.getProperty("java.io.tmpdir"), false);
            directory.close();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open the lucene directory", e);
        }
        if (!directory.getClass().getName().equals(getFSDirectoryClass())) {
            throw new SearchEngineException("Setting type of FS directory is a JVM "
                    + "level setting, you can not set different values within the same JVM");
        }
        super.configure(searchEngineFactory, settings, mapping);
    }

    protected String getFSDirectoryClass() {
        return "org.apache.lucene.store.FSDirectory";
    }

    protected Directory doGetDirectoryForPath(String path, boolean create) throws SearchEngineException {
        try {
            return FSDirectory.getDirectory(indexPath + "/" + path, create);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open directory for path [" + path + "]", e);
        }
    }

    public void deleteIndex() throws SearchEngineException {
        File indexPathFile = new File(indexPath);
        if (indexPathFile.exists()) {
            boolean deleted = LuceneUtils.deleteDir(indexPathFile);
            if (!deleted) {
                throw new SearchEngineException("Failed to delete index directory [" + indexPath + "]");
            }
        }
    }

    protected CopyFromHolder doBeforeCopyFrom() throws SearchEngineException {
        // first rename the current index directory
        File indexPathFile = new File(indexPath);
        int count = 0;
        File renameToIndexPathFile;
        while (true) {
            renameToIndexPathFile = new File(indexPath + "copy" + count++);
            if (!renameToIndexPathFile.exists()) {
                break;
            }
        }
        if (!indexPathFile.renameTo(renameToIndexPathFile)) {
            throw new SearchEngineException("Failed to rename index [" + indexPath
                    + "] to [" + renameToIndexPathFile.getPath() + "]");
        }
        CopyFromHolder holder = new CopyFromHolder();
        holder.createOriginalDirectory = true;
        holder.data = renameToIndexPathFile;
        return holder;
    }

    protected void doAfterSuccessfulCopyFrom(CopyFromHolder holder) throws SearchEngineException {
        File renameToIndexPathFile = (File) holder.data;
        try {
            LuceneUtils.deleteDir(renameToIndexPathFile);
        } catch (Exception e) {
            log.warn("Failed to delete backup directory", e);
        }
    }

    protected void doAfterFailedCopyFrom(Object holder) throws SearchEngineException {
        // TODO if it fails, try to rename the original one back
    }
}
