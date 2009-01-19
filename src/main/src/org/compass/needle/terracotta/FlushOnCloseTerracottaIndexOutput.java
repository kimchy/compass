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

import org.apache.lucene.store.IndexOutput;

/**
 * A terracotta index output that only adds the file when the output is closed.
 *
 * @author kimchy
 */
public class FlushOnCloseTerracottaIndexOutput extends IndexOutput {

    private final TerracottaDirectory dir;

    private final int bufferSize;

    private final String name;

    private TerracottaFile file;

    private byte[] currentBuffer;

    private int currentBufferIndex;

    private int bufferPosition;

    private long bufferStart;

    private int bufferLength;

    private boolean open;

    FlushOnCloseTerracottaIndexOutput(TerracottaDirectory dir, String name) throws IOException {
        this.dir = dir;
        this.name = name;
        this.bufferSize = dir.getBufferSize();
        file = new TerracottaFile();

        // just add an empty file to mark the fact we created a file for this name
        dir.addFile(name, TerracottaDirectory.EMPTY_FILE);
        
        open = true;
        // make sure that we switch to the
        // first needed buffer lazily
        currentBufferIndex = -1;
        currentBuffer = null;
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == bufferLength) {
            currentBufferIndex++;
            switchCurrentBuffer();
        }
        currentBuffer[bufferPosition++] = b;
    }

    public void writeBytes(byte[] b, int offset, int len) throws IOException {
        while (len > 0) {
            if (bufferPosition == bufferLength) {
                currentBufferIndex++;
                switchCurrentBuffer();
            }

            int remainInBuffer = currentBuffer.length - bufferPosition;
            int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
            System.arraycopy(b, offset, currentBuffer, bufferPosition, bytesToCopy);
            offset += bytesToCopy;
            len -= bytesToCopy;
            bufferPosition += bytesToCopy;
        }
    }

    public long getFilePointer() {
        return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
    }

    public void flush() throws IOException {
        setFileLength();
    }

    public void close() throws IOException {
        if (!open) {
            return;
        }
        open = false;
        // flush any buffer we might have
        flush();
        file.setLastModified(System.currentTimeMillis());
        dir.addFile(name, file);
        currentBuffer = null;
    }

    public long length() {
        return file.getLength();
    }

    public void seek(long pos) throws IOException {
        // set the file length in case we seek back
        // and flush() has not been called yet
        setFileLength();
        if (pos < bufferStart || pos >= bufferStart + bufferLength) {
            currentBufferIndex = (int) (pos / bufferSize);
            switchCurrentBuffer();
        }

        bufferPosition = (int) (pos % bufferSize);
    }

    private void switchCurrentBuffer() throws IOException {
        if (currentBufferIndex == file.getNumBuffers()) {
            currentBuffer = file.addBuffer(bufferSize);
        } else {
            currentBuffer = file.getBuffer(currentBufferIndex);
        }
        bufferPosition = 0;
        bufferStart = (long) bufferSize * (long) currentBufferIndex;
        bufferLength = currentBuffer.length;
    }

    private void setFileLength() {
        long pointer = bufferStart + bufferPosition;
        if (pointer > file.length) {
            file.setLength(pointer);
        }
    }
}