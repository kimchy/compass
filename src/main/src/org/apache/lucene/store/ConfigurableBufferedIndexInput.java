package org.apache.lucene.store;

import java.io.IOException;

/**
 * A simple base class that performs index input memory based buffering. Allows the buffer size to be
 * configurable.
 *
 * @author kimchy
 */
// NEED TO BE MONITORED AGAINST LUCENE
public abstract class ConfigurableBufferedIndexInput extends IndexInput {

    /**
     * The default value for the buffer size (in bytes). Currently 1024.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    protected byte[] buffer;

    protected long bufferStart = 0;              // position in file of buffer
    protected int bufferLength = 0;              // end of valid bytes
    protected int bufferPosition = 0;          // next byte to read

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    protected void initBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public byte readByte() throws IOException {
        if (bufferPosition >= bufferLength)
            refill();
        return buffer[bufferPosition++];
    }

    public void readBytes(byte[] b, int offset, int len)
            throws IOException {
        if (len < bufferSize) {
            for (int i = 0; i < len; i++)          // read byte-by-byte
                b[i + offset] = readByte();
        } else {                      // read all-at-once
            long start = getFilePointer();
            seekInternal(start);
            readInternal(b, offset, len);

            bufferStart = start + len;          // adjust stream variables
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read
        }
    }

    protected void refill() throws IOException {
        long start = bufferStart + bufferPosition;
        long end = start + bufferSize;
        if (end > length())                  // don't read past EOF
            end = length();
        bufferLength = (int) (end - start);
        if (bufferLength <= 0)
            throw new IOException("read past EOF");

        if (buffer == null)
            buffer = new byte[bufferSize];          // allocate buffer lazily
        readInternal(buffer, 0, bufferLength);

        bufferStart = start;
        bufferPosition = 0;
    }

    /**
     * Expert: implements buffer refill.  Reads bytes from the current position
     * in the input.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param length the number of bytes to read
     */
    protected abstract void readInternal(byte[] b, int offset, int length)
            throws IOException;

    public long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    public void seek(long pos) throws IOException {
        if (pos >= bufferStart && pos < (bufferStart + bufferLength))
            bufferPosition = (int) (pos - bufferStart);  // seek within buffer
        else {
            bufferStart = pos;
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read()
            seekInternal(pos);
        }
    }

    /**
     * Expert: implements seek.  Sets current position in this file, where the
     * next {@link #readInternal(byte[],int,int)} will occur.
     *
     * @see #readInternal(byte[],int,int)
     */
    protected abstract void seekInternal(long pos) throws IOException;

    public Object clone() {
        ConfigurableBufferedIndexInput clone = (ConfigurableBufferedIndexInput) super.clone();

        if (buffer != null) {
            clone.buffer = new byte[bufferSize];
            System.arraycopy(buffer, 0, clone.buffer, 0, bufferLength);
        }

        return clone;
    }


}
