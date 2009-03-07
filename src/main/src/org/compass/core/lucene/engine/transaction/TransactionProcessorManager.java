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

package org.compass.core.lucene.engine.transaction;

import java.util.HashMap;
import java.util.Map;

import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassMappingAware;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.lucene.LuceneTransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.mt.MTTransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.search.SearchTransactionProcessorFactory;
import org.compass.core.util.ClassUtils;

/**
 * Transaction Processor Manager holds a map of name to {@link org.compass.core.lucene.engine.transaction.TransactionProcessorFactory}
 * mapping.
 *
 * @author kimchy
 */
public class TransactionProcessorManager {

    private final Map<String, TransactionProcessorFactory> transactionProcessors = new HashMap<String, TransactionProcessorFactory>();

    public TransactionProcessorManager(LuceneSearchEngineFactory searchEngineFactory) {
        CompassSettings settings = searchEngineFactory.getLuceneSettings().getSettings();
        Map<String, CompassSettings> processorsGroupsSettings = settings.getSettingGroups(LuceneEnvironment.Transaction.Processor.PREFIX);
        for (Map.Entry<String, CompassSettings> entry : processorsGroupsSettings.entrySet()) {
            Object type = entry.getValue().getSetting(LuceneEnvironment.Transaction.Processor.CONFIG_TYPE);
            if (type == null) {
                // probably one of the defualt, will be taken into accoutn when created
                continue;
            }
            if (type instanceof String) {
                String typeClass = (String) type;
                if (typeClass.equalsIgnoreCase(LuceneEnvironment.Transaction.Processor.ReadCommitted.NAME)) {
                    type = new ReadCommittedTransactionProcessorFactory();
                } else if (typeClass.equalsIgnoreCase(LuceneEnvironment.Transaction.Processor.Lucene.NAME)) {
                    type = new LuceneTransactionProcessorFactory();
                } else if (typeClass.equalsIgnoreCase(LuceneEnvironment.Transaction.Processor.Async.NAME)) {
                    type = new AsyncTransactionProcessorFactory();
                } else {
                    try {
                        type = ClassUtils.forName(typeClass, settings.getClassLoader()).newInstance();
                    } catch (Exception e) {
                        throw new ConfigurationException("Failed to create custom transaction procesor factory class [" + typeClass + "]", e);
                    }
                }
            }
            if (!(type instanceof TransactionProcessorFactory)) {
                throw new ConfigurationException("Transaction processor factory [" + type + "] is not of type [" + TransactionProcessorFactory.class.getName() + "]");
            }
            if (type instanceof CompassMappingAware) {
                ((CompassMappingAware) type).setCompassMapping(searchEngineFactory.getMapping());
            }
            if (type instanceof SearchEngineFactoryAware) {
                ((SearchEngineFactoryAware) type).setSearchEngineFactory(searchEngineFactory);
            }
            if (type instanceof CompassConfigurable) {
                ((CompassConfigurable) type).configure(searchEngineFactory.getLuceneSettings().getSettings());
            }
            transactionProcessors.put(entry.getKey(), (TransactionProcessorFactory) type);
        }
        addDefaulIfRequired(searchEngineFactory, LuceneEnvironment.Transaction.Processor.ReadCommitted.NAME, ReadCommittedTransactionProcessorFactory.class);
        addDefaulIfRequired(searchEngineFactory, LuceneEnvironment.Transaction.Processor.Lucene.NAME, LuceneTransactionProcessorFactory.class);
        addDefaulIfRequired(searchEngineFactory, LuceneEnvironment.Transaction.Processor.Async.NAME, AsyncTransactionProcessorFactory.class);
        addDefaulIfRequired(searchEngineFactory, LuceneEnvironment.Transaction.Processor.Search.NAME, SearchTransactionProcessorFactory.class);
        addDefaulIfRequired(searchEngineFactory, LuceneEnvironment.Transaction.Processor.MT.NAME, MTTransactionProcessorFactory.class);
    }

    private void addDefaulIfRequired(LuceneSearchEngineFactory searchEngineFactory, String key, Class<? extends TransactionProcessorFactory> type) {
        if (transactionProcessors.containsKey(key)) {
            return;
        }
        TransactionProcessorFactory processorFactory;
        try {
            processorFactory = type.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create instanco of [" + type.getName() + "]", e);
        }
        if (processorFactory instanceof CompassMappingAware) {
            ((CompassMappingAware) processorFactory).setCompassMapping(searchEngineFactory.getMapping());
        }
        if (processorFactory instanceof SearchEngineFactoryAware) {
            ((SearchEngineFactoryAware) processorFactory).setSearchEngineFactory(searchEngineFactory);
        }
        if (processorFactory instanceof CompassConfigurable) {
            ((CompassConfigurable) processorFactory).configure(searchEngineFactory.getLuceneSettings().getSettings());
        }
        transactionProcessors.put(key, processorFactory);
    }

    public void close() {
        for (TransactionProcessorFactory transactionProcessorFactory : transactionProcessors.values()) {
            transactionProcessorFactory.close();
        }
    }


    public TransactionProcessorFactory getProcessorFactory(String name) {
        TransactionProcessorFactory processorFactory = transactionProcessors.get(name);
        if (processorFactory == null) {
            throw new SearchEngineException("Faile to find transaction processor factory bounded under [" + name + "]");
        }
        return processorFactory;
    }
}
