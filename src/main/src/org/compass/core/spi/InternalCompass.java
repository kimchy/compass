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

package org.compass.core.spi;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.events.CompassEventManager;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.transaction.TransactionFactory;

public interface InternalCompass extends Compass {

    void start();

    void stop();

    CompassSession openSession(boolean allowCreate, boolean checkClosed);

    String getName();

    CompassSettings getSettings();

    CompassMapping getMapping();

    ExecutorManager getExecutorManager();

    CompassMetaData getMetaData();

    SearchEngineFactory getSearchEngineFactory();

    TransactionFactory getTransactionFactory();

    LocalTransactionFactory getLocalTransactionFactory();

    ConverterLookup getConverterLookup();

    PropertyNamingStrategy getPropertyNamingStrategy();

    CompassEventManager getEventManager();

    void addRebuildEventListener(RebuildEventListener eventListener);

    void removeRebuildEventListener(RebuildEventListener eventListener);
}
