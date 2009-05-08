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
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author kimchy
 */
public class JarScanner implements Scanner {

    private ZipInputStream jar;

    private ZipEntry next;

    private Filter filter;

    private boolean initial = true;

    private boolean closed = false;

    public JarScanner(String basePackage, InputStream is, Filter filter) throws IOException {
        this.filter = new JarPackageFilterWrapper(basePackage, filter);
        jar = new JarInputStream(is);
    }

    private void setNext() {
        initial = true;
        try {
            if (next != null) jar.closeEntry();
            next = null;
            do {
                next = jar.getNextEntry();
            } while (next != null && (next.isDirectory() || (filter == null || !filter.accepts(next.getName()))));
            if (next == null) {
                close();
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to browse jar", e);
        }
    }

    public ScanItem next() {
        if (closed || (next == null && !initial)) return null;
        setNext();
        if (next == null) return null;
        return new ScanItem(new InputStreamWrapper(jar), next.getName());
    }

    public void close() {
        try {
            closed = true;
            jar.close();
        } catch (IOException ignored) {
            // do nothing
        }

    }

    private class JarPackageFilterWrapper implements Filter {

        private String basePackage;

        private Filter delegate;

        private JarPackageFilterWrapper(String basePackage, Filter delegate) {
            this.basePackage = basePackage;
            this.delegate = delegate;
        }

        public boolean accepts(String name) {
            if (!name.startsWith(basePackage)) {
                return false;
            }
            return delegate.accepts(name.substring(basePackage.length() + 1));
        }
    }
}
