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

package org.compass.core.lucene.engine.all;

import org.apache.lucene.index.Payload;

/**
 * Utility to write float into and from byte array.
 *
 * @author kimchy
 */
public class AllBoostUtils {

    public final static int _7BIT = 0x7F;
    public final static int _6BIT = 0x3F;
    public final static int _5BIT = 0x1F;
    public final static int _4BIT = 0x0F;
    public final static int BITN7 = 0x40;
    public final static int BITN8 = 0x80;

    public final static int FORBYTES = 0xffff;

    public final static int _8_7_BIT = 0xC0;
    public final static int _8_7_6_BIT = 0xE0;
    public final static int _SECOND_WORD = 0xF0;

    public final static int _FULLY = 64;

    public final static int _MAX_LEN = 32;

    private AllBoostUtils() {

    }

    public static float readFloat(byte[] content) {
        int data = readInt(content);
        return Float.intBitsToFloat(data);
    }

    public static Payload writeFloat(float data) {
        int idata = Float.floatToIntBits(data);
        return writeInt(idata);
    }

    public static int readInt(byte[] content) {
        int index = 0;
        int mbyte = content[index++];
        int num = mbyte & _6BIT;
        int len = 6;
        boolean sign = (mbyte & BITN7) != 0;

        while ((mbyte & BITN8) != 0) {
            mbyte = content[index++];
            num |= (mbyte & _7BIT) << len;
            len += 7;
        }

        if (sign)
            num = ~num;

        return num;
    }

    public static Payload writeInt(int data) {
        byte buffer[] = new byte[_MAX_LEN];
        int size = 0;
        int mbyte = 0;
        if (data < 0) {
            mbyte = _FULLY;
            data = ~data;
        }
        mbyte |= (byte) (data & _6BIT);
        for (data >>>= 6; data != 0; data >>>= 7) {
            mbyte |= BITN8;
            buffer[size++] = (byte) mbyte;
            mbyte = data & _7BIT;
        }

        if (size == 0) {
            return new Payload(new byte[]{(byte) mbyte});
        } else {
            buffer[size++] = (byte) mbyte;
            return new Payload(buffer, 0, size);
        }
    }

}
