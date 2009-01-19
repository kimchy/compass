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

package org.apache.lucene.store.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.DirectoryTemplate;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;

/**
 * @author kimchy
 */
public class SimpleVsTests extends AbstractJdbcDirectoryTests {

    private boolean DISABLE = true;

    private Directory fsDir;
    private Directory ramDir;
    private Directory jdbcDir;
    private Collection docs = loadDocuments(3000, 5);
    private boolean useCompoundFile = false;

    protected void setUp() throws Exception {
        super.setUp();
        String fsIndexDir =
                System.getProperty("java.io.tmpdir", "tmp") +
                        System.getProperty("file.separator") + "fs-index";

        ramDir = new RAMDirectory();
        fsDir = FSDirectory.getDirectory(fsIndexDir, true);
        jdbcDir = new JdbcDirectory(dataSource, createDialect(), "TEST");
        
        Connection con = DataSourceUtils.getConnection(dataSource);
        ((JdbcDirectory) jdbcDir).create();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
    }

    public void testTiming() throws IOException {
        if (DISABLE) {
            return;
        }
        long ramTiming = timeIndexWriter(ramDir);
        long fsTiming = timeIndexWriter(fsDir);
        long jdbcTiming = timeIndexWriter(jdbcDir);

        assertTrue(fsTiming > ramTiming);

        System.out.println("RAMDirectory Time: " + (ramTiming) + " ms");
        System.out.println("FSDirectory Time : " + (fsTiming) + " ms");
        System.out.println("JdbcDirectory Time : " + (jdbcTiming) + " ms");
    }

    private long timeIndexWriter(Directory dir) throws IOException {
        long start = System.currentTimeMillis();
        addDocuments(dir);
        long stop = System.currentTimeMillis();
        return (stop - start);
    }

    private void addDocuments(Directory dir) throws IOException {
        DirectoryTemplate template = new DirectoryTemplate(dir);
        template.execute(new DirectoryTemplate.DirectoryCallbackWithoutResult() {
            protected void doInDirectoryWithoutResult(Directory dir) throws IOException {
                IndexWriter writer = new IndexWriter(dir, new SimpleAnalyzer(), true);
                writer.setUseCompoundFile(useCompoundFile);

                /**
                 // change to adjust performance of indexing with FSDirectory
                 writer.mergeFactor = writer.mergeFactor;
                 writer.maxMergeDocs = writer.maxMergeDocs;
                 writer.minMergeDocs = writer.minMergeDocs;
                 */

                for (Iterator iter = docs.iterator(); iter.hasNext();) {
                    Document doc = new Document();
                    String word = (String) iter.next();
                    doc.add(new Field("keyword", word, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    doc.add(new Field("unindexed", word, Field.Store.YES, Field.Index.NO));
                    doc.add(new Field("unstored", word, Field.Store.NO, Field.Index.TOKENIZED));
                    doc.add(new Field("text", word, Field.Store.YES, Field.Index.TOKENIZED));
                    writer.addDocument(doc);
                }
                writer.optimize();
                writer.close();
            }
        });
    }

    private Collection loadDocuments(int numDocs, int wordsPerDoc) {
        Collection docs = new ArrayList(numDocs);
        for (int i = 0; i < numDocs; i++) {
            StringBuffer doc = new StringBuffer(wordsPerDoc);
            for (int j = 0; j < wordsPerDoc; j++) {
                doc.append("Bibamus ");
            }
            docs.add(doc.toString());
        }
        return docs;
    }

}
