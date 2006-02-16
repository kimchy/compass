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

package org.compass.core.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A <code>Reader</code> implementation which wraps several
 * <code>Reader</code>s and reads them sequentially.
 * 
 * @author kimchy
 */
public class MultiIOReader extends Reader {

    private ArrayList readers = new ArrayList();

    private Reader currentReader;

    private int index = 0;

    public MultiIOReader() {
    }

    public MultiIOReader(Reader reader) {
        add(reader);
    }

    public MultiIOReader(Reader[] readers) {
        for (int i = 0; i < readers.length; i++) {
            add(readers[i]);
        }
    }

    public void add(Reader reader) {
        if (reader == null) {
            return;
        }
        readers.add(reader);
        if (currentReader == null) {
            currentReader = reader;
        }
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (readers == null)
            throw new IOException("Stream closed");
    }

    public int read() throws IOException {
        ensureOpen();
        int retVal = -1;

        while (retVal == -1) {
            retVal = currentReader.read();
            if (retVal == -1) {
                ++index;
                if (index == readers.size()) {
                    return -1;
                }
                currentReader = (Reader) readers.get(index);
            }
        }
        return retVal;
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        ensureOpen();
        int sizeRead = -1;
        while (sizeRead == -1) {
            sizeRead = currentReader.read(cbuf, off, len);
            if (sizeRead == -1) {
                ++index;
                if (index == readers.size()) {
                    return -1;
                }
                currentReader = (Reader) readers.get(index);
            }
        }
        return sizeRead;
    }

    public void close() throws IOException {
        // the reader is closed, no need to close it again
        if (readers == null) {
            return;
        }

        IOException ioe = null;
        for (Iterator it = readers.iterator(); it.hasNext();) {
            try {
                ((Reader) it.next()).close();
            } catch (IOException e) {
                ioe = e;
            }
        }
        readers = null;
        if (ioe != null) {
            throw ioe;
        }
    }
}
