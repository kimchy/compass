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

/**
 * Simpler iterator than java.util.iterator.
 *
 * @author kimchy
 */
public interface Scanner {

    /**
     * User is resposible for closing the InputStream returned
     *
     * @return null if no more streams left to iterate on
     */
    ScanItem next();

    /**
     * Cleanup any open resources of the iterator
     */
    void close();
}
