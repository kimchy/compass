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

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import org.compass.core.util.reader.MultiIOReader;

/**
 * @author kimchy
 */
public class MultiReaderTests extends TestCase {

    public void testRead() throws Exception {
        Reader[] readers = new Reader[] { new StringReader("01234"), new StringReader("5678"), new StringReader("9") };
        Reader r = new MultiIOReader(readers);
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 48, r.read());
        }
        assertEquals(-1, r.read());

        readers = new Reader[] { new StringReader("0"), new StringReader("1"), new StringReader("2"),
                new StringReader("3"), new StringReader("4"), new StringReader("5"), new StringReader("6"),
                new StringReader("7") };
        r = new MultiIOReader(readers);
        for (int i = 0; i < 8; i++) {
            assertEquals(i + 48, r.read());
        }
        assertEquals(-1, r.read());

        readers = new Reader[] { new StringReader("01234"), new StringReader(""), null, new StringReader("") };
        r = new MultiIOReader(readers);
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 48, r.read());
        }
        assertEquals(-1, r.read());
    }

    public void testReadBuffer() throws Exception {
        Reader[] readers = new Reader[] { new StringReader("01234"), new StringReader("5678"), new StringReader("9") };
        Reader r = new MultiIOReader(readers);
        char[] buffer = new char[3];
        int found = 0;
        int index = 0;
        while (found != -1) {
            found = r.read(buffer);
            for (int i = 0; i < found; i++) {
                assertEquals(index + 48, buffer[i]);
                index++;
            }
        }
    }
}
