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

import junit.framework.TestCase;
import org.compass.core.util.reader.ReverseStringReader;

/**
 * @author kimchy
 */
public class ReverseStringReaderTests extends TestCase {

    public void testRead() throws Exception {
        String str = "123";

        ReverseStringReader reader = new ReverseStringReader(str);
        assertEquals('3', reader.read());
        assertEquals('2', reader.read());
        assertEquals('1', reader.read());
        assertEquals(-1, reader.read());

    }

    public void testReadBuffer() throws Exception {
        String str = "123456789";

        ReverseStringReader reader = new ReverseStringReader(str);
        char[] data = new char[5];
        reader.read(data);
        assertEquals('9', data[0]);
        assertEquals('8', data[1]);
        assertEquals('7', data[2]);
        assertEquals('6', data[3]);
        assertEquals('5', data[4]);

        reader.read(data);
        assertEquals('4', data[0]);
        assertEquals('3', data[1]);
        assertEquals('2', data[2]);
        assertEquals('1', data[3]);
        assertEquals('5', data[4]);
    }
}
