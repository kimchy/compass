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

package org.compass.core.lucene.engine.store;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.util.FileSystemUtils;

/**
 * A directory store implemented using a file system. Uses Lucene {@link org.apache.lucene.store.FSDirectory}.
 *
 * @author kimchy
 */
public class FSDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(FSDirectoryStore.class);

    public static final String PROTOCOL = "file://";

    private String indexPath;

    public void configure(CompassSettings settings) throws CompassException {
        String connection = settings.getSetting(CompassEnvironment.CONNECTION);
        indexPath = findIndexPath(connection);
        // Make sure we use the FSDirectory
        System.setProperty("org.apache.lucene.FSDirectory.class", getFSDirectoryClass());
        FSDirectory directory;
        try {
            directory = FSDirectory.getDirectory(System.getProperty("java.io.tmpdir"));
            directory.close();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open the lucene directory", e);
        }
        if (!directory.getClass().getName().equals(getFSDirectoryClass())) {
            throw new SearchEngineException("Setting type of FS directory is a JVM "
                    + "level setting, you can not set different values within the same JVM");
        }
    }

    protected String findIndexPath(String connection) {
        if (connection.startsWith(PROTOCOL)) {
            return connection.substring(PROTOCOL.length());
        }
        return connection;
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        try {
            String path = buildPath(subContext, subIndex);
            Directory dir =  FSDirectory.getDirectory(path);
            dir.setLockFactory(new SimpleFSLockFactory(path));
            return dir;
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open directory for path [" + subIndex + "]", e);
        }
    }

    @Override
    public String[] listSubIndexes(String subContext) throws SearchEngineException, UnsupportedOperationException {
        File subContextIndex = new File(indexPath + "/" + subContext);
        if (!subContextIndex.exists()) {
            return null;
        }
        File[] subIndexFiles = subContextIndex.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        String[] subIndexes = new String[subIndexFiles.length];
        for (int i = 0; i < subIndexFiles.length; i++) {
            subIndexes[i] = subIndexFiles[i].getName();
        }
        return subIndexes;
    }

    @Override
    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        File indexPathFile = new File(buildPath(subContext, subIndex));
        if (indexPathFile.exists()) {
            boolean deleted = false;
            // do this retries for windows...
            for (int i = 0; i < 10; i++) {
                deleted = FileSystemUtils.deleteRecursively(indexPathFile);
                if (deleted) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // no matter
                }
            }
            if (!deleted) {
                throw new SearchEngineException("Failed to delete index directory [" + buildPath(subContext, subIndex) + "]");
            }
        }
    }

    @Override
    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        deleteIndex(dir, subContext, subIndex);
    }

    @Override
    public CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir) throws SearchEngineException {
        // first rename the current index directory
        String path = buildPath(subContext, subIndex);
        File indexPathFile = new File(path);
        int count = 0;
        File renameToIndexPathFile;
        while (true) {
            renameToIndexPathFile = new File(path + "-copy" + count++);
            if (!renameToIndexPathFile.exists()) {
                break;
            }
        }
        boolean renamed = false;
        for (int i = 0; i < 10; i++) {
            renamed = indexPathFile.renameTo(renameToIndexPathFile);
            if (!renamed) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // do nothing
                }
            } else {
                break;
            }
        }
        if (!renamed) {
            throw new SearchEngineException("Failed to rename index [" + path
                    + "] to [" + renameToIndexPathFile.getPath() + "]");
        }

        ((FSDirectory) dir).getFile().mkdirs();

        CopyFromHolder holder = new CopyFromHolder();
        holder.data = renameToIndexPathFile;
        return holder;
    }

    @Override
    public void afterSuccessfulCopyFrom(String subContext, String subIndex, CopyFromHolder holder) throws SearchEngineException {
        File renameToIndexPathFile = (File) holder.data;
        try {
            FileSystemUtils.deleteRecursively(renameToIndexPathFile);
        } catch (Exception e) {
            log.warn("Failed to delete backup directory", e);
        }
    }

    @Override
    public void afterFailedCopyFrom(String subContext, String subIndex, CopyFromHolder holder) throws SearchEngineException {
        // TODO if it fails, try to rename the original one back
    }

    protected String buildPath(String subContext, String subIndex) {
        return indexPath + "/" + subContext + "/" + subIndex;
    }

    @Override
    public boolean suggestedUseCompoundFile() {
        return true;
    }

    protected String getFSDirectoryClass() {
        return "org.apache.lucene.store.FSDirectory";
    }
}
