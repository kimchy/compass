/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.needle.terracotta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.terracotta.modules.concurrent.collections.ConcurrentStringMap;

/**
 * An extension to {@link TerracottaDirectory} that works in much the same way, except that it
 * uses Terracotta {@link ConcurrentStringMap} instead of {@link ConcurrentHashMap} to store the
 * index at.
 *
 * @author kimchy
 */
public class CSMTerracottaDirectory extends TerracottaDirectory {

    public CSMTerracottaDirectory() {
        super();
    }

    public CSMTerracottaDirectory(int bufferSize, int flushRate) {
        super(bufferSize, flushRate);
    }

    @Override
    protected Map<String, TerracottaFile> createMap(int chmInitialCapacity, float chmLoadFactor, int chmConcurrencyLevel) {
        return new ConcurrentStringMap<TerracottaFile>();
    }
}
