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
public class StringWithSeparatorReader extends Reader implements RepeatableReader {

    private String str;
    private char separator;
    private int length;
    private int next = 0;
    private boolean readSeparator = false;

    public StringWithSeparatorReader(String s, char separator) {
        this.str = s;
        this.separator = separator;
        this.length = s.length();
    }

    public int read() {
        if (next >= length) {
            if (readSeparator) {
                return -1;
            }
            readSeparator = true;
            return separator;
        }
        return str.charAt(next++);
    }

    public int read(char cbuf[], int off, int len) {
        if (len == 0) {
            return 0;
        }
        if (next >= length) {
            if (readSeparator) {
                // reset the reader for next reads
                close();
                // return -1 to indicate no more data
                return -1;
            }
            readSeparator = true;
            cbuf[off] = separator;
            return 1;
        }
        int leftOver = length - next;
        if (leftOver >= len) {
            str.getChars(next, next + len, cbuf, off);
            next += len;
            return len;
        } else {
            str.getChars(next, next + leftOver, cbuf, off);
            next += leftOver;
            readSeparator = true;
            cbuf[off + leftOver] = separator;
            return leftOver + 1;
        }
    }

    public long skip(long ns) {
        throw new UnsupportedOperationException("Skip not supported");
    }

    public boolean ready() {
        return true;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) {
        throw new UnsupportedOperationException("Skip not supported");
    }

    public void reset() {
        throw new UnsupportedOperationException("Skip not supported");
    }

    public void close() {
        next = 0;
        readSeparator = false;
    }

}
