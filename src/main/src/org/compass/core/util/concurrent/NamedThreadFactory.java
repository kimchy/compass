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

package org.compass.core.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * A thread factory that accepts a name prefix and if the thread is going to be a daemon thread or not.
 *
 * @author kimchy
 */
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

    private final String name;

    private final boolean daemon;

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread t = defaultThreadFactory.newThread(r);
        t.setDaemon(daemon);
        t.setName(name + " [" + t.getName() + "]");
        return t;
    }
}
