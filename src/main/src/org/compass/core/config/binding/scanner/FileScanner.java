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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * A scanner that works either on a file or a directory. Handles the "file" protocol.
 *
 * @author kimchy
 */
public class FileScanner implements Scanner {

    private ArrayList<File> files;

    private int index = 0;

    public FileScanner(String basePackge, File file, Filter filter) {
        files = new ArrayList<File>();
        try {
            create(files, file, new FileFilterWrapper(basePackge, filter));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void create(List<File> list, File dir, Filter filter) throws Exception {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                create(list, file, filter);
            } else {
                if (filter == null || filter.accepts(file.getAbsolutePath())) {
                    list.add(file);
                }
            }
        }
    }

    public ScanItem next() {
        if (index >= files.size()) return null;
        File fp = files.get(index++);
        try {
            return new ScanItem(new FileInputStream(fp), fp.getAbsolutePath());
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
    }

    private class FileFilterWrapper implements Filter {

        private String basePackage;

        private Filter delegate;

        private FileFilterWrapper(String basePackage, Filter delegate) {
            this.basePackage = basePackage;
            this.delegate = delegate;
        }

        public boolean accepts(String name) {
            name = name.replace('\\', '/');
            int index = name.lastIndexOf(basePackage);
            if (index == -1) {
                return false;
            }
            return delegate.accepts(name.substring(index + basePackage.length() + 1));
        }
    }
}
