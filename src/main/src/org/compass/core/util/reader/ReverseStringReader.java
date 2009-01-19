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

package org.compass.core.util.reader;

import java.io.IOException;
import java.io.Reader;

import org.compass.core.engine.RepeatableReader;


/**
 * A character stream whose source is a string. reverses the string. Allows for
 * repeatable reads from the same string.
 * <p/>
 * Note, this is an unsafe reader in terms of {@link IndexOutOfBoundsException}.
 *
 * @author kimchy
 */
public class ReverseStringReader extends Reader implements RepeatableReader {

    private String str;
    private int next = 0;
    private int mark = 0;

    public ReverseStringReader(String s) {
        this.str = s;
        this.next = s.length();
    }

    public int read() throws IOException {
        if (next <= 0)
            return -1;
        return str.charAt(--next);
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (next <= 0) {
            // reset the repeatable reader
            close();
            // return -1 indicating nothing left to read
            return -1;
        }
        int n = Math.min(next, len);
        for (int i = 0; i < n; i++) {
            cbuf[off + i] = (char) read();
        }
        return n;
    }

    public long skip(long ns) throws IOException {
        if (next <= 0)
            return 0;
        long n = Math.min(next, ns);
        next -= n;
        return n;
    }

    public boolean ready() throws IOException {
        return true;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        mark = next;
    }

    public void reset() throws IOException {
        next = mark;
    }

    // as part of the repeatable reader, the close will actually return the reader to it's
    // initial state
    public void close() {
        this.next = str.length();
    }

}
