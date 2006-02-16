/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.lucene.engine.store;

public class LuceneSearchEngineStoreFactory {

    public static final String MEM_PREFIX = "ram://";

    public static final String FILE_PREFIX = "file://";

    public static final String MMAP_PREFIX = "mmap://";

    public static final String JDBC_PREFIX = "jdbc://";

    public static LuceneSearchEngineStore createStore(String connection, String subContext) {
        if (connection.startsWith(MEM_PREFIX)) {
            return new RAMLuceneSearchEngineStore(connection.substring(MEM_PREFIX.length(), connection.length()), subContext);
        }
        if (connection.startsWith(MMAP_PREFIX)) {
            return new MMapLuceneSearchEngineStore(connection.substring(MMAP_PREFIX.length(), connection.length()), subContext);
        }
        if (connection.startsWith(FILE_PREFIX)) {
            return new FSLuceneSearchEngineStore(connection.substring(FILE_PREFIX.length(), connection.length()), subContext);
        }
        if (connection.startsWith(JDBC_PREFIX)) {
            return new JdbcLuceneSearchEngineStore(connection.substring(JDBC_PREFIX.length(), connection.length()), subContext);
        }
        return new FSLuceneSearchEngineStore(connection, subContext);
    }
}
