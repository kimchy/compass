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

package org.compass.needle.gigaspaces.store;

import java.io.IOException;

/**
 * An exception marking a problem with the GigaSpace directory.
 *
 * @author kimchy
 */
public class GigaSpaceDirectoryException extends IOException {

    public GigaSpaceDirectoryException(String message) {
        super(message);
    }

    public GigaSpaceDirectoryException(String message, Exception e) {
        super(message);
        initCause(e);
    }

    public GigaSpaceDirectoryException(String indexName, String fileName, String message) {
        super("Index [" + indexName + "] file [" + fileName + "]: " + message);
    }

    public GigaSpaceDirectoryException(String indexName, String fileName, String message, Exception e) {
        super("Index [" + indexName + "] file [" + fileName + "]: " + message);
        initCause(e);
    }
}
