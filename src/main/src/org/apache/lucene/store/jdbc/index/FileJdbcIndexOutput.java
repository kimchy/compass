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

package org.apache.lucene.store.jdbc.index;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;

/**
 * An <code>IndexOutput</code> implemenation that writes all the data to a temporary file, and when closed, flushes
 * the file to the database.
 * <p/>
 * Usefull for large files that are known in advance to be larger then the acceptable threshold configured in
 * {@link RAMAndFileJdbcIndexOutput}.
 *
 * @author kimchy
 */
public class FileJdbcIndexOutput extends AbstractJdbcIndexOutput {

    private RandomAccessFile file = null;

    private File tempFile;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        super.configure(name, jdbcDirectory, settings);
        tempFile = File.createTempFile(jdbcDirectory.getTable().getName() + "_" + name + "_" + System.currentTimeMillis(), ".ljt");
        this.file = new RandomAccessFile(tempFile, "rw");
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
    }

    protected void flushBuffer(byte[] b, int offset, int len) throws IOException {
        file.write(b, offset, len);
    }

    /**
     * Random-access methods
     */
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public long length() throws IOException {
        return file.length();
    }

    protected InputStream openInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(file.getFD()));
    }


    protected void doBeforeClose() throws IOException {
        file.seek(0);
    }


    protected void doAfterClose() throws IOException {
        file.close();
        tempFile.delete();
        tempFile = null;
        file = null;
    }
}
