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

package org.compass.core.util;

import java.io.IOException;

import junit.framework.TestCase;
import org.compass.core.util.reader.StringWithSeparatorReader;

/**
 * @author kimchy
 */
public class StringWithSeparatorReaderTests extends TestCase {

    public void testSingleRead() {
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 48, reader.read());
        }
        assertEquals(32, reader.read());
        assertEquals(-1, reader.read());
    }

    public void testBufferReadWithSingleCharBuffer() throws IOException {
        char[] buff = new char[1];
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        for (int i = 0; i < 7; i++) {
            assertEquals(1, reader.read(buff));
            assertEquals(i + 48, buff[0]);
        }
        assertEquals(1, reader.read(buff));
        assertEquals(32, buff[0]);
        assertEquals(-1, reader.read(buff));
    }

    public void testBufferReadWithExactBuffer() throws IOException {
        char[] buff = new char[8];
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        assertEquals(8, reader.read(buff));
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 48, buff[i]);
        }
        assertEquals(32, buff[7]);
        assertEquals(-1, reader.read(buff));
    }

    public void testBufferReadWithExactNoSeparatorBuffer() throws IOException {
        char[] buff = new char[7];
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        assertEquals(7, reader.read(buff));
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 48, buff[i]);
        }
        assertEquals(1, reader.read(buff));
        assertEquals(32, buff[0]);
        assertEquals(-1, reader.read(buff));
    }

    public void testBufferReadWithHalfSizeBuffer() throws IOException {
        char[] buff = new char[4];
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        assertEquals(4, reader.read(buff));
        for (int i = 0; i < 4; i++) {
            assertEquals(i + 48, buff[i]);
        }
        assertEquals(4, reader.read(buff));
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 52, buff[i]);
        }
        assertEquals(32, buff[3]);
        assertEquals(-1, reader.read(buff));
    }

    public void testBufferReadWithExactBufferRepeatableRead() throws IOException {
        char[] buff = new char[8];
        StringWithSeparatorReader reader = new StringWithSeparatorReader("0123456", ' ');
        assertEquals(8, reader.read(buff));
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 48, buff[i]);
        }
        assertEquals(32, buff[7]);
        assertEquals(-1, reader.read(buff));
        reader.close();
        assertEquals(8, reader.read(buff));
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 48, buff[i]);
        }
        assertEquals(32, buff[7]);
        assertEquals(-1, reader.read(buff));
    }
}
