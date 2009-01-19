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

package org.compass.core.config.binding.scanner;

import java.io.IOException;
import java.io.InputStream;

/**
 * Delegate to everything but close().  This object will not close the stream
 *
 * @author kimchy
 */
public class InputStreamWrapper extends InputStream {

    private InputStream delegate;

    public InputStreamWrapper(InputStream delegate) {
        this.delegate = delegate;
    }

    public int read()
            throws IOException {
        return delegate.read();
    }

    public int read(byte[] bytes)
            throws IOException {
        return delegate.read(bytes);
    }

    public int read(byte[] bytes, int i, int i1)
            throws IOException {
        return delegate.read(bytes, i, i1);
    }

    public long skip(long l)
            throws IOException {
        return delegate.skip(l);
    }

    public int available()
            throws IOException {
        return delegate.available();
    }

    public void close()
            throws IOException {
        // ignored
    }

    public void mark(int i) {
        delegate.mark(i);
    }

    public void reset()
            throws IOException {
        delegate.reset();
    }

    public boolean markSupported() {
        return delegate.markSupported();
    }
}
