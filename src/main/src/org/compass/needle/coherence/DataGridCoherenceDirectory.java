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

package org.compass.needle.coherence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 * The default coherence directory allowing to store Lucene index within Coherence directory.
 *
 * <p>The implementation uses {@link org.compass.needle.coherence.FileHeaderKey} and
 * {@link org.compass.needle.coherence.FileHeaderValue} as the header information for a file
 * (such as size and timestamp), and includes one or more buckets using
 * {@link org.compass.needle.coherence.FileBucketKey} and {@link org.compass.needle.coherence.FileBucketValue}.
 *
 * <p>Locking is done using {@link DefaultCoherenceLockFactory}.
 *
 * <p>Note, if possible with the coherence edition, it is preferable to use {@link InvocableCoherenceDirectory}.
 *
 * @author kimchy
 */
public class DataGridCoherenceDirectory extends CoherenceDirectory {

    public static final int DEFAULT_BUCKET_SIZE = 20 * 1024;

    public static final int DEFAULT_FLUSH_RATE = 50;

    private String indexName;

    private NamedCache cache;

    private int bucketSize = DEFAULT_BUCKET_SIZE;

    private int flushRate = DEFAULT_FLUSH_RATE;

    private boolean closeCache = false;

    private ValueExtractor indexNameKeyExtractor = new KeyExtractor("getIndexName");

    private ValueExtractor fileNameKeyExtractor = new KeyExtractor("getFileName");

    private ValueExtractor typeKeyExtractor = new KeyExtractor("getType");

    private Filter indexNameEqualsFilter;

    private Filter listFilter;

    // we store an on going list of created index outputs since Lucene needs them
    // *before* it closes the index output. It calls fileExists in the middle.
    private transient Map<String, IndexOutput> onGoingIndexOutputs = new ConcurrentHashMap<String, IndexOutput>();

    public DataGridCoherenceDirectory(String cacheName) {
        this(cacheName, cacheName, DEFAULT_BUCKET_SIZE);
    }

    public DataGridCoherenceDirectory(String cacheName, String indexName) {
        this(cacheName, indexName, DEFAULT_BUCKET_SIZE);
    }

    public DataGridCoherenceDirectory(String cacheName, String indexName, int bucketSize) {
        this(CacheFactory.getCache(cacheName), indexName, bucketSize);
        this.closeCache = true;
    }

    public DataGridCoherenceDirectory(NamedCache cache, String indexName) {
        this(cache, indexName, DEFAULT_BUCKET_SIZE);
    }

    public DataGridCoherenceDirectory(NamedCache cache, String indexName, int bucketSize) {
        this(cache, indexName, bucketSize, DEFAULT_FLUSH_RATE);
    }

    public DataGridCoherenceDirectory(NamedCache cache, String indexName, int bucketSize, int flushRate) {
        this.indexName = indexName;
        this.cache = cache;
        this.bucketSize = bucketSize;
        this.flushRate = flushRate;
        this.closeCache = false;
        // init indexes 
        cache.addIndex(indexNameKeyExtractor, false, null);
        cache.addIndex(typeKeyExtractor, false, null);
        cache.addIndex(fileNameKeyExtractor, false, null);
        // init filters
        indexNameEqualsFilter = new EqualsFilter(getIndexNameKeyExtractor(), getIndexName());
        listFilter = new AndFilter(indexNameEqualsFilter, new EqualsFilter(getTypeKeyExtractor(), FileKey.FILE_HEADER));
        setLockFactory(new DefaultCoherenceLockFactory(getCache(), getIndexName()));
        // call a possible doInit by subclasses
        doInit();
    }

    protected void doInit() {

    }

    public String getIndexName() {
        return indexName;
    }

    public NamedCache getCache() {
        return cache;
    }

    public int getBucketSize() {
        return this.bucketSize;
    }

    public int getFlushRate() {
        return this.flushRate;
    }

    public Map<String, IndexOutput> getOnGoingIndexOutputs() {
        return onGoingIndexOutputs;
    }

    public ValueExtractor getIndexNameKeyExtractor() {
        return indexNameKeyExtractor;
    }

    public ValueExtractor getTypeKeyExtractor() {
        return typeKeyExtractor;
    }

    public ValueExtractor getFileNameKeyExtractor() {
        return fileNameKeyExtractor;
    }

    public Filter getIndexNameEqualsFilter() {
        return indexNameEqualsFilter;
    }

    public boolean fileExists(String name) throws IOException {
        if (onGoingIndexOutputs.containsKey(name)) {
            return true;
        }
        return cache.containsKey(new FileHeaderKey(indexName, name));
    }

    public long fileModified(String name) throws IOException {
        FileHeaderValue fileHeaderValue = (FileHeaderValue) cache.get(new FileHeaderKey(indexName, name));
        if (fileHeaderValue != null) {
            return fileHeaderValue.getLastModified();
        }
        return 0;
    }

    public void touchFile(String name) throws IOException {
        FileHeaderKey fileHeaderKey = new FileHeaderKey(indexName, name);
        FileHeaderValue fileHeaderValue = (FileHeaderValue) cache.get(fileHeaderKey);
        if (fileHeaderValue != null) {
            fileHeaderValue.touch();
        } else {
            fileHeaderValue = new FileHeaderValue(System.currentTimeMillis(), 0);
        }
        cache.put(fileHeaderKey, fileHeaderValue);
    }

    public void deleteFile(String name) throws IOException {
        cache.remove(new FileHeaderKey(indexName, name));
        // iterate through the entries and remove them until we get null
        // not using a filter to get the keys since we can do without it (I don't see a removeAll mehtod).
        // still, one of the problems with this is the fact that it returns the old value
        int bucketIndex = 0;
        while (true) {
            FileBucketValue fileBucketValue = (FileBucketValue) cache.remove(new FileBucketKey(indexName, name, bucketIndex++));
            if (fileBucketValue == null) {
                // we hit the end, bail
                break;
            }
        }
    }

    public void renameFile(String from, String to) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
        FileHeaderValue fileHeaderValue = (FileHeaderValue) cache.get(new FileHeaderKey(indexName, name));
        if (fileHeaderValue != null) {
            return fileHeaderValue.getSize();
        }
        return 0;
    }

    public String[] list() throws IOException {
        Set fileHeaders = getCache().keySet(listFilter);
        ArrayList<String> fileNames = new ArrayList<String>();
        for (Iterator it = fileHeaders.iterator(); it.hasNext();) {
            Object key = it.next();
            fileNames.add(((FileHeaderKey) key).getFileName());
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }

    public void deleteContent() {
        Set keys = getCache().keySet(indexNameEqualsFilter);
        for (Iterator it = keys.iterator(); it.hasNext();) {
            // a bit crappy, we need to remove each one and it returns the old content
            getCache().remove(it.next());
        }
    }

    public IndexOutput createOutput(String name) throws IOException {
        IndexOutput indexOutput;
        if (LuceneFileNames.isSegmentsFile(name)) {
            indexOutput = new FlushOnCloseCoherenceIndexOutput(this, name);
        } else {
            indexOutput = new CoherenceMemIndexOutput(this, name);
        }
        onGoingIndexOutputs.put(name, indexOutput);
        return indexOutput;
    }

    public IndexInput openInput(String name) throws IOException {
        FileHeaderKey fileHeaderKey = new FileHeaderKey(indexName, name);
        FileHeaderValue fileHeaderValue = (FileHeaderValue) cache.get(fileHeaderKey);
        if (fileHeaderValue == null) {
            throw new IOException("Failed to find file " + fileHeaderKey);
        }
        return new CoherenceIndexInput(this, fileHeaderKey, fileHeaderValue);
    }

    public void close() throws IOException {
        // TODO should we #destroy() here?
        if (closeCache) {
            cache.release();
        }
    }


}
