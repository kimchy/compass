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

package org.compass.core.converter.basic.format;

import java.text.ParseException;

import org.compass.core.converter.ConversionException;

/**
 * Wrapper around {@link Formatter} that can be called by multiple
 * threads concurrently.
 *
 * <p>Format has a high overhead in creating and is not thread safe. To make best
 * use of resources, the ThreadSafeFormat provides a dynamically sizing pool of
 * instances, each of which will only be called by a single thread at a time.
 *
 * <p>The pool has a maximum capacity, to limit overhead. If all instances in the
 * pool are in use and another is required, it shall block until one becomes
 * available.
 *
 * @author kimchy
 */
public class ThreadSafeFormat implements Formatter {

    private final int initialPoolSize;

    private final int maxPoolSize;

    private transient Formatter[] pool;

    private int nextAvailable = 0;

    private final Object mutex = new Object();

    private final FormatterFactory formatterFactory;

    public ThreadSafeFormat(int initialPoolSize, int maxPoolSize, FormatterFactory formatterFactory) {
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.formatterFactory = formatterFactory;
    }

    public boolean isThreadSafe() {
        return true;
    }

    public String format(Object obj) {
        Formatter format = fetchFromPool();
        try {
            return format.format(obj);
        } finally {
            putInPool(format);
        }
    }

    public Object parse(String str) throws ParseException {
        Formatter format = fetchFromPool();
        try {
            return format.parse(str);
        } finally {
            putInPool(format);
        }
    }

    private Formatter fetchFromPool() {
        Formatter result;
        synchronized (mutex) {
            if (pool == null) {
                nextAvailable = -1;
                pool = new Formatter[maxPoolSize];
                for (int i = 0; i < initialPoolSize; i++) {
                    putInPool(formatterFactory.create());
                }
            }
            while (nextAvailable < 0) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new ConversionException("Interrupted whilst waiting for a free item in the pool", e);
                }
            }
            result = pool[nextAvailable];
            nextAvailable--;
        }
        if (result == null) {
            result = formatterFactory.create();
            putInPool(result);
        }
        return result;
    }

    private void putInPool(Formatter format) {
        synchronized (mutex) {
            nextAvailable++;
            pool[nextAvailable] = format;
            mutex.notify();
        }
    }

}
