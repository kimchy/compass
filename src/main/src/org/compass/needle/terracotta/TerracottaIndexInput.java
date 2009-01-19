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

package org.compass.needle.terracotta;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

/**
 * @author kimchy
 */
public class TerracottaIndexInput extends IndexInput implements Cloneable {

    final int bufferSize;

    private TerracottaFile file;
    private long length;
    private int numberOfBuffers;

    private byte[] currentBuffer;
    private int currentBufferIndex;

    private int bufferPosition;
    private long bufferStart;
    private int bufferLength;

    TerracottaIndexInput(TerracottaFile f, int bufferSize) throws IOException {
        this.bufferSize = bufferSize;
        file = f;
        length = file.length;
        numberOfBuffers = file.getNumBuffers();
        if (length / this.bufferSize >= Integer.MAX_VALUE) {
            throw new IOException("Too large File! " + length);
        }

        // make sure that we switch to the
        // first needed buffer lazily
        currentBufferIndex = -1;
        currentBuffer = null;
    }

    public void close() {
        // nothing to do here
    }

    public long length() {
        return length;
    }

    public byte readByte() throws IOException {
        if (bufferPosition >= bufferLength) {
            currentBufferIndex++;
            switchCurrentBuffer();
        }
        return currentBuffer[bufferPosition++];
    }

    public void readBytes(byte[] b, int offset, int len) throws IOException {
        while (len > 0) {
            if (bufferPosition >= bufferLength) {
                currentBufferIndex++;
                switchCurrentBuffer();
            }

            int remainInBuffer = bufferLength - bufferPosition;
            int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
            System.arraycopy(currentBuffer, bufferPosition, b, offset, bytesToCopy);
            offset += bytesToCopy;
            len -= bytesToCopy;
            bufferPosition += bytesToCopy;
        }
    }

    private void switchCurrentBuffer() throws IOException {
        if (currentBufferIndex >= numberOfBuffers) {
            // end of file reached, no more buffers left
            throw new IOException("Read past EOF");
        } else {
            currentBuffer = file.getBuffer(currentBufferIndex);
            bufferPosition = 0;
            bufferStart = (long) bufferSize * (long) currentBufferIndex;
            long buflen = length - bufferStart;
            bufferLength = buflen > bufferSize ? bufferSize : (int) buflen;
        }
    }

    public long getFilePointer() {
        return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
    }

    public void seek(long pos) throws IOException {
        if (currentBuffer == null || pos < bufferStart || pos >= bufferStart + bufferSize) {
            currentBufferIndex = (int) (pos / bufferSize);
            switchCurrentBuffer();
        }
        bufferPosition = (int) (pos % bufferSize);
    }
}
