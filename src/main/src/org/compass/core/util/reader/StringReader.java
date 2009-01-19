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

import java.io.Reader;

import org.compass.core.engine.RepeatableReader;


/**
 * A character stream whose source is a string. Allows for
 * repeatable reads from the same string.
 * <p/>
 * Note, this is an unsafe reader in terms of {@link IndexOutOfBoundsException}.
 *
 * @author kimchy
 */
public class StringReader extends Reader implements RepeatableReader {

    private String str;
    private int length;
    private int next = 0;
    private int mark = 0;

    public StringReader(String s) {
        this.str = s;
        this.length = s.length();
    }

    public int read() {
        if (next >= length)
            return -1;
        return str.charAt(next++);
    }

    public int read(char cbuf[], int off, int len) {
        if (len == 0) {
            return 0;
        }
        if (next >= length) {
            // reset the reader for a possible next read
            close();
            // and return -1 to indicate no more data
            return -1;
        }
        int n = Math.min(length - next, len);
        str.getChars(next, next + n, cbuf, off);
        next += n;
        return n;
    }

    public long skip(long ns) {
        if (next >= length)
            return 0;
        long n = Math.min(length - next, ns);
        next += n;
        return n;
    }

    public boolean ready() {
        return true;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        mark = next;
    }

    public void reset() {
        next = mark;
    }

    public void close() {
        next = 0;
        mark = 0;
    }

}
