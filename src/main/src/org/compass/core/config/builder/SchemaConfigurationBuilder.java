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

package org.compass.core.config.builder;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.DomUtils;
import org.compass.core.util.SystemPropertyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author kimchy
 */
public class SchemaConfigurationBuilder extends AbstractXmlConfigurationBuilder {

    protected void doProcess(Document doc, CompassConfiguration config) throws ConfigurationException {
        Element root = doc.getDocumentElement();
        // the root is the compass element
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                if ("compass".equals(node.getLocalName())) {
                    processCompass((Element) node, config);
                }
            }
        }
    }

    public void processCompass(Element compassElement, CompassConfiguration config) {

        config.getSettings().setSetting(CompassEnvironment.NAME, DomUtils.getElementAttribute(compassElement, "name"));

        NodeList nl = compassElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String nodeName = ele.getLocalName();
                String methodName = "bind" +
                        Character.toUpperCase(nodeName.charAt(0)) + nodeName.substring(1, nodeName.length());
                Method method;
                try {
                    method = SchemaConfigurationBuilder.class.getMethod(methodName,
                            new Class[]{Element.class, CompassConfiguration.class});
                } catch (NoSuchMethodException e) {
                    throw new ConfigurationException("Compass failed to process node [" + nodeName + "], this is " +
                            "either a mailformed xml configuration (not validated against the xsd), or an internal" +
                            " bug in compass");
                }
                try {
                    method.invoke(this, new Object[]{ele, config});
                } catch (InvocationTargetException e) {
                    throw new ConfigurationException("Failed to invoke binding metod for node [" + nodeName + "]", e.getTargetException());
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException("Failed to access binding metod for node [" + nodeName + "]", e);
                }
            }
        }
    }

    private String getElementAttribute(Element ele, String name) {
        return SystemPropertyUtils.resolvePlaceholders(DomUtils.getElementAttribute(ele, name));
    }

    private String getElementAttribute(Element ele, String name, String defaultValue) {
        return SystemPropertyUtils.resolvePlaceholders(DomUtils.getElementAttribute(ele, name, defaultValue));
    }

    private boolean getElementAttributeAsBoolean(Element ele, String name, boolean defaultValue) {
        String sValue = getElementAttribute(ele, name);
        if (sValue == null) {
            return defaultValue;
        }
        return Boolean.valueOf(sValue).booleanValue();
    }

    public void bindOsem(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        settings.setSetting(CompassEnvironment.Osem.MANAGED_ID_INDEX, getElementAttribute(ele, "managedIdIndex"));
        settings.setSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, getElementAttribute(ele, "supportUnmarshall"));
    }

    public void bindConverters(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        List convertersEle = DomUtils.getChildElementsByTagName(ele, "converter", true);
        for (Iterator it = convertersEle.iterator(); it.hasNext();) {
            Element converterEle = (Element) it.next();
            SettingsHolder settingsHolder = processSettings(converterEle);
            settingsHolder.names.add(CompassEnvironment.Converter.TYPE);
            settingsHolder.values.add(getElementAttribute(converterEle, "type"));
            settings.setGroupSettings(CompassEnvironment.Converter.PREFIX, getElementAttribute(converterEle, "name"),
                    settingsHolder.names(), settingsHolder.values());
        }
    }

    public void bindPropertyAccessors(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        List propertyAccessorsEle = DomUtils.getChildElementsByTagName(ele, "propertyAccessor", true);
        for (Iterator it = propertyAccessorsEle.iterator(); it.hasNext();) {
            Element propertyAccessorEle = (Element) it.next();
            SettingsHolder settingsHolder = processSettings(propertyAccessorEle);
            settingsHolder.names.add(CompassEnvironment.PropertyAccessor.TYPE);
            settingsHolder.values.add(getElementAttribute(propertyAccessorEle, "type"));
            settings.setGroupSettings(CompassEnvironment.PropertyAccessor.PREFIX, getElementAttribute(propertyAccessorEle, "name"),
                    settingsHolder.names(), settingsHolder.values());
        }
    }

    public void bindPropertyNamingStrategy(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        String type = getElementAttribute(ele, "type");
        if (type == null) {
            type = getElementAttribute(ele, "typeClass");
        }
        settings.setSetting(CompassEnvironment.NamingStrategy.TYPE, type);
    }

    public void bindSearchEngine(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.USE_COMPOUND_FILE, getElementAttribute(ele, "useCompoundFile"));
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.MAX_FIELD_LENGTH, getElementAttribute(ele, "maxFieldLength"));
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.CACHE_INTERVAL_INVALIDATION, getElementAttribute(ele, "cacheInvalidationInterval"));
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, getElementAttribute(ele, "cacheAsyncInvalidation"));
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.INDEX_MANAGER_SCHEDULE_INTERVAL, getElementAttribute(ele, "indexManagerScheduleInterval"));
        settings.setSetting(LuceneEnvironment.SearchEngineIndex.WAIT_FOR_CACHE_INVALIDATION_ON_INDEX_OPERATION, getElementAttribute(ele, "waitForCacheInvalidationOnIndexOperation"));
        settings.setSetting(LuceneEnvironment.DEFAULT_SEARCH, getElementAttribute(ele, "defaultSearch"));
        List child = DomUtils.getChildElementsByTagName(ele, "aliasProperty", true);
        if (child.size() == 1) {
            Element aliasPropertyEle = (Element) child.get(0);
            settings.setSetting(CompassEnvironment.Alias.NAME, getElementAttribute(aliasPropertyEle, "name"));
        }
        child = DomUtils.getChildElementsByTagName(ele, "allProperty", true);
        if (child.size() == 1) {
            Element allPropertyEle = (Element) child.get(0);
            settings.setSetting(CompassEnvironment.All.NAME, getElementAttribute(allPropertyEle, "name"));
            settings.setSetting(CompassEnvironment.All.TERM_VECTOR, getElementAttribute(allPropertyEle, "termVector"));
            settings.setSetting(CompassEnvironment.All.ENABLED, getElementAttribute(allPropertyEle, "enabled"));
            settings.setSetting(CompassEnvironment.All.BOOST_SUPPORT, getElementAttribute(allPropertyEle, "boostSupport"));
            settings.setSetting(CompassEnvironment.All.INCLUDE_UNMAPPED_PROPERTIES, getElementAttribute(allPropertyEle, "includeUnmappedProperties"));
        }
        child = DomUtils.getChildElementsByTagName(ele, "optimizer", true);
        if (child.size() == 1) {
            Element optimizerEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.Optimizer.TYPE, getElementAttribute(optimizerEle, "type"));
            settings.setSetting(LuceneEnvironment.Optimizer.SCHEDULE, getElementAttribute(optimizerEle, "schedule"));
            settings.setSetting(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, getElementAttribute(optimizerEle, "scheduleInterval"));
            settings.setSetting(LuceneEnvironment.Optimizer.MAX_NUMBER_OF_SEGMENTS, getElementAttribute(optimizerEle, "maxNumberOfSegments"));
        }
        child = DomUtils.getChildElementsByTagName(ele, "highlighter", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element highlighterEle = (Element) it.next();
            String highlighterName = getElementAttribute(highlighterEle, "name");
            SettingsHolder settingsHolder = processSettings(highlighterEle);

            settingsHolder.names.add(LuceneEnvironment.Highlighter.TEXT_TOKENIZER);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "textTokenizer"));

            settingsHolder.names.add(LuceneEnvironment.Highlighter.REWRITE_QUERY);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "rewriteQuery"));

            settingsHolder.names.add(LuceneEnvironment.Highlighter.COMPUTE_IDF);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "computeIdf"));

            settingsHolder.names.add(LuceneEnvironment.Highlighter.MAX_NUM_FRAGMENTS);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "maxNumFragments"));

            settingsHolder.names.add(LuceneEnvironment.Highlighter.SEPARATOR);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "separator"));

            settingsHolder.names.add(LuceneEnvironment.Highlighter.MAX_BYTES_TO_ANALYZE);
            settingsHolder.values.add(getElementAttribute(highlighterEle, "maxBytesToAnalyze"));

            List fragmenterList = DomUtils.getChildElementsByTagName(highlighterEle, "fragmenter", true);
            if (fragmenterList.size() == 1) {
                Element fragmenterEle = (Element) fragmenterList.get(0);
                String type = getElementAttribute(fragmenterEle, "type");
                if ("custom".equals(type)) {
                    type = getElementAttribute(fragmenterEle, "class");
                }
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Fragmenter.TYPE);
                settingsHolder.values.add(type);
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Fragmenter.SIMPLE_SIZE);
                settingsHolder.values.add(getElementAttribute(fragmenterEle, "size"));
            }

            List encoderList = DomUtils.getChildElementsByTagName(highlighterEle, "encoder", true);
            if (encoderList.size() == 1) {
                Element encoderEle = (Element) encoderList.get(0);
                String type = getElementAttribute(encoderEle, "type");
                if ("custom".equals(type)) {
                    type = getElementAttribute(encoderEle, "class");
                }
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Encoder.TYPE);
                settingsHolder.values.add(type);
            }

            List formatterList = DomUtils.getChildElementsByTagName(highlighterEle, "simpleFormatter", true);
            if (formatterList.size() == 1) {
                Element formatterEle = (Element) formatterList.get(0);
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.TYPE);
                settingsHolder.values.add(LuceneEnvironment.Highlighter.Formatter.SIMPLE);

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.SIMPLE_PRE_HIGHLIGHT);
                settingsHolder.values.add(getElementAttribute(formatterEle, "pre"));

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.SIMPLE_POST_HIGHLIGHT);
                settingsHolder.values.add(getElementAttribute(formatterEle, "post"));
            }

            formatterList = DomUtils.getChildElementsByTagName(highlighterEle, "htmlSpanGradientFormatter", true);
            if (formatterList.size() == 1) {
                Element formatterEle = (Element) formatterList.get(0);
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.TYPE);
                settingsHolder.values.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT);

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_SCORE);
                settingsHolder.values.add(getElementAttribute(formatterEle, "maxScore"));

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MIN_FOREGROUND_COLOR);
                settingsHolder.values.add(getElementAttribute(formatterEle, "minForegroundColor"));

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_FOREGROUND_COLOR);
                settingsHolder.values.add(getElementAttribute(formatterEle, "maxForegroundColor"));

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MIN_BACKGROUND_COLOR);
                settingsHolder.values.add(getElementAttribute(formatterEle, "minBackgroundColor"));

                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_BACKGROUND_COLOR);
                settingsHolder.values.add(getElementAttribute(formatterEle, "maxBackgroundColor"));
            }

            formatterList = DomUtils.getChildElementsByTagName(highlighterEle, "customFormatter", true);
            if (formatterList.size() == 1) {
                Element formatterEle = (Element) formatterList.get(0);
                settingsHolder.names.add(LuceneEnvironment.Highlighter.Formatter.TYPE);
                settingsHolder.values.add(getElementAttribute(formatterEle, "class"));
            }

            settings.setGroupSettings(LuceneEnvironment.Highlighter.PREFIX, highlighterName,
                    settingsHolder.names(), settingsHolder.values());
        }
        child = DomUtils.getChildElementsByTagName(ele, "analyzer", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element analyzerEle = (Element) it.next();
            String analyzerName = getElementAttribute(analyzerEle, "name");
            SettingsHolder settingsHolder = processSettings(analyzerEle);
            String analyzerType = getElementAttribute(analyzerEle, "type");
            if (analyzerType != null) {
                if (analyzerType.equals("CustomAnalyzer")) {
                    analyzerType = getElementAttribute(analyzerEle, "analyzerClass");
                    if (analyzerType == null) {
                        throw new ConfigurationException("Analyzer [" + analyzerName + "] has " +
                                "type of [CustomAnalyzer] but does not set analyzerClass");
                    }
                }
                settingsHolder.names.add(LuceneEnvironment.Analyzer.TYPE);
                settingsHolder.values.add(analyzerType);

                if (analyzerType.equals("Snowball")) {
                    settingsHolder.names.add(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
                    settingsHolder.values.add(getElementAttribute(analyzerEle, "snowballType"));
                }
            }
            settingsHolder.names.add(LuceneEnvironment.Analyzer.FILTERS);
            settingsHolder.values.add(getElementAttribute(analyzerEle, "filters"));

            List stopWordsList = DomUtils.getChildElementsByTagName(analyzerEle, "stopWords", true);
            if (stopWordsList.size() == 1) {
                Element stopWordsEle = (Element) stopWordsList.get(0);
                StringBuffer sb = new StringBuffer();
                boolean replace = getElementAttributeAsBoolean(stopWordsEle, "replace", false);
                if (!replace) {
                    sb.append("+");
                }
                List stopWords = DomUtils.getChildElementsByTagName(stopWordsEle, "stopWord", true);
                for (Iterator swIt = stopWords.iterator(); swIt.hasNext();) {
                    Element stopWordEle = (Element) swIt.next();
                    sb.append(getElementAttribute(stopWordEle, "value")).append(",");
                }
                settingsHolder.names.add(LuceneEnvironment.Analyzer.STOPWORDS);
                settingsHolder.values.add(sb.toString());
            }
            settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, analyzerName,
                    settingsHolder.names(), settingsHolder.values());
        }
        child = DomUtils.getChildElementsByTagName(ele, "analyzerFilter", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element analyzerFilterEle = (Element) it.next();
            SettingsHolder settingsHolder = processSettings(analyzerFilterEle);
            settingsHolder.names.add(LuceneEnvironment.AnalyzerFilter.TYPE);
            settingsHolder.values.add(getElementAttribute(analyzerFilterEle, "type"));
            settings.setGroupSettings(LuceneEnvironment.AnalyzerFilter.PREFIX, getElementAttribute(analyzerFilterEle, "name"),
                    settingsHolder.names(), settingsHolder.values());
        }
        child = DomUtils.getChildElementsByTagName(ele, "queryParser", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element queryParserEle = (Element) it.next();
            SettingsHolder settingsHolder = processSettings(queryParserEle);
            settingsHolder.names.add(LuceneEnvironment.QueryParser.TYPE);
            settingsHolder.values.add(getElementAttribute(queryParserEle, "type"));
            settings.setGroupSettings(LuceneEnvironment.QueryParser.PREFIX, getElementAttribute(queryParserEle, "name"),
                    settingsHolder.names(), settingsHolder.values());
        }
        child = DomUtils.getChildElementsByTagName(ele, "indexDeletionPolicy", true);
        if (child.size() == 1) {
            Element indexDeletionPolicyEle = (Element) child.get(0);
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "keepLastCommit", true);
            if (child.size() == 1) {
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, LuceneEnvironment.IndexDeletionPolicy.KeepLastCommit.NAME);
            }
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "keepAll", true);
            if (child.size() == 1) {
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, LuceneEnvironment.IndexDeletionPolicy.KeepAll.NAME);
            }
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "keepLastN", true);
            if (child.size() == 1) {
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, LuceneEnvironment.IndexDeletionPolicy.KeepLastN.NAME);
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.KeepLastN.NUM_TO_KEEP, ((Element) child.get(0)).getAttribute("numToKeep"));
            }
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "keepNoneOnInit", true);
            if (child.size() == 1) {
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, LuceneEnvironment.IndexDeletionPolicy.KeepNoneOnInit.NAME);
            }
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "expirationTime", true);
            if (child.size() == 1) {
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.NAME);
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.ExpirationTime.EXPIRATION_TIME_IN_SECONDS, ((Element) child.get(0)).getAttribute("expirationTimeSeconds"));
            }
            child = DomUtils.getChildElementsByTagName(indexDeletionPolicyEle, "custom", true);
            if (child.size() == 1) {
                Element customEle = ((Element) child.get(0));
                settings.setSetting(LuceneEnvironment.IndexDeletionPolicy.TYPE, customEle.getAttribute("type"));
                bindSettings(customEle, config);
            }
        }
    }

    public void bindCache(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        List child = DomUtils.getChildElementsByTagName(ele, "firstLevel", true);
        if (child.size() == 1) {
            Element firstLevelCacheEle = (Element) child.get(0);
            settings.setSetting(CompassEnvironment.Cache.FirstLevel.TYPE, getElementAttribute(firstLevelCacheEle, "type"));
        }
    }

    public void bindTransaction(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        settings.setSetting(LuceneEnvironment.Transaction.Processor.TYPE, getElementAttribute(ele, "processor"));
        settings.setSetting(CompassEnvironment.Transaction.FACTORY, getElementAttribute(ele, "factory"));
        settings.setSetting(CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, getElementAttribute(ele, "commitBeforeCompletion"));
        settings.setSetting(LuceneEnvironment.Transaction.LOCK_TIMEOUT, getElementAttribute(ele, "lockTimeout"));
        settings.setSetting(LuceneEnvironment.Transaction.LOCK_POLL_INTERVAL, getElementAttribute(ele, "lockPollInterval"));
        settings.setSetting(CompassEnvironment.Transaction.DISABLE_AUTO_JOIN_SESSION, getElementAttribute(ele, "disableAutoJoinSession"));
        List child = DomUtils.getChildElementsByTagName(ele, "jtaSettings", true);
        if (child.size() == 1) {
            Element jtaSettingsEle = (Element) child.get(0);
            settings.setSetting(CompassEnvironment.Transaction.USER_TRANSACTION, getElementAttribute(jtaSettingsEle, "userTransactionName"));
            settings.setSetting(CompassEnvironment.Transaction.CACHE_USER_TRANSACTION, getElementAttribute(jtaSettingsEle, "cacheUserTransaction"));
            settings.setSetting(CompassEnvironment.Transaction.MANAGER_LOOKUP, getElementAttribute(jtaSettingsEle, "managerLookup"));
            settings.setSetting(CompassEnvironment.Transaction.MANAGER_LOOKUP, getElementAttribute(jtaSettingsEle, "managerLookupClass"));
        }
        child = DomUtils.getChildElementsByTagName(ele, "processors", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element prEle = (Element) it.next();
            List child1 = DomUtils.getChildElementsByTagName(prEle, "readCommitted", true);
            if (child1.size() == 1) {
                Element readCommittedSettingsEle = (Element) child1.get(0);
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.TransLog.CONNECTION, getElementAttribute(readCommittedSettingsEle, "transLog"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.TransLog.OPTIMIZE_TRANS_LOG, getElementAttribute(readCommittedSettingsEle, "optimizeTransLog"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.CONCURRENT_OPERATIONS, getElementAttribute(readCommittedSettingsEle, "concurrentOperations"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.CONCURRENCY_LEVEL, getElementAttribute(readCommittedSettingsEle, "concurrencyLevel"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.HASHING, getElementAttribute(readCommittedSettingsEle, "hashing"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.ADD_TIMEOUT, getElementAttribute(readCommittedSettingsEle, "addTimeout"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.ReadCommitted.BACKLOG, getElementAttribute(readCommittedSettingsEle, "backlog"));
            }
            child1 = DomUtils.getChildElementsByTagName(prEle, "lucene", true);
            if (child1.size() == 1) {
                Element luceneSettingsEle = (Element) child1.get(0);
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Lucene.CONCURRENT_OPERATIONS, getElementAttribute(luceneSettingsEle, "concurrentOperations"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Lucene.CONCURRENCY_LEVEL, getElementAttribute(luceneSettingsEle, "concurrencyLevel"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Lucene.HASHING, getElementAttribute(luceneSettingsEle, "hashing"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Lucene.ADD_TIMEOUT, getElementAttribute(luceneSettingsEle, "addTimeout"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Lucene.BACKLOG, getElementAttribute(luceneSettingsEle, "backlog"));
            }
            child1 = DomUtils.getChildElementsByTagName(prEle, "async", true);
            if (child1.size() == 1) {
                Element asyncSettingsEle = (Element) child1.get(0);
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.ADD_TIMEOUT, getElementAttribute(asyncSettingsEle, "addTimeout"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.BACKLOG, getElementAttribute(asyncSettingsEle, "backlog"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, getElementAttribute(asyncSettingsEle, "batchJobSize"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_TIMEOUT, getElementAttribute(asyncSettingsEle, "batchJobTimeout"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.CONCURRENCY_LEVEL, getElementAttribute(asyncSettingsEle, "concurrencyLevel"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.HASHING, getElementAttribute(asyncSettingsEle, "hashing"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.NON_BLOCKING_BATCH_JOBS_SIZE, getElementAttribute(asyncSettingsEle, "nonBlockingBatchJobSize"));
                settings.setSetting(LuceneEnvironment.Transaction.Processor.Async.PROCESS_BEFORE_CLOSE, getElementAttribute(asyncSettingsEle, "processBeforeClose"));
            }
        }
    }

    public void bindConnection(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();

        // directory wrapper providers
        List child = DomUtils.getChildElementsByTagName(ele, "directoryWrapperProvider", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element dwEle = (Element) it.next();
            SettingsHolder settingsHolder = processSettings(dwEle);
            settingsHolder.names.add(LuceneEnvironment.DirectoryWrapper.TYPE);
            settingsHolder.values.add(getElementAttribute(dwEle, "type"));
            settings.setGroupSettings(LuceneEnvironment.DirectoryWrapper.PREFIX,
                    getElementAttribute(dwEle, "name"),
                    settingsHolder.names(), settingsHolder.values());
        }

        child = DomUtils.getChildElementsByTagName(ele, "localCache", true);
        for (Iterator it = child.iterator(); it.hasNext();) {
            Element localCacheEle = (Element) it.next();
            String subIndex = localCacheEle.getAttribute("subIndex");
            String connection = localCacheEle.getAttribute("connection");
            settings.setGroupSettings(LuceneEnvironment.LocalCache.PREFIX, subIndex,
                    new String[]{LuceneEnvironment.LocalCache.CONNECTION},
                    new String[]{connection});
        }

        child = DomUtils.getChildElementsByTagName(ele, "lockFactory", true);
        if (child.size() == 1) {
            Element lockFactoryEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.LockFactory.TYPE, getElementAttribute(lockFactoryEle, "type"));
            settings.setSetting(LuceneEnvironment.LockFactory.PATH, getElementAttribute(lockFactoryEle, "path"));
        }

        child = DomUtils.getChildElementsByTagName(ele, "file", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String path = getElementAttribute(connEle, "path");
            if (!path.startsWith("file://")) {
                path = "file://" + path;
            }
            settings.setSetting(CompassEnvironment.CONNECTION, path);
            return;
        }
        child = DomUtils.getChildElementsByTagName(ele, "mmap", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String path = getElementAttribute(connEle, "path");
            if (!path.startsWith("mmap://")) {
                path = "mmap://" + path;
            }
            settings.setSetting(CompassEnvironment.CONNECTION, path);
            return;
        }
        child = DomUtils.getChildElementsByTagName(ele, "niofs", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String path = getElementAttribute(connEle, "path");
            if (!path.startsWith("niofs://")) {
                path = "niofs://" + path;
            }
            settings.setSetting(CompassEnvironment.CONNECTION, path);
            return;
        }
        // --- RAM Connection ---
        child = DomUtils.getChildElementsByTagName(ele, "ram", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String path = getElementAttribute(connEle, "path");
            if (!path.startsWith("ram://")) {
                path = "ram://" + path;
            }
            settings.setSetting(CompassEnvironment.CONNECTION, path);
            return;
        }
        // --- Space Connection ---
        child = DomUtils.getChildElementsByTagName(ele, "space", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String url = getElementAttribute(connEle, "url");
            String indexName = getElementAttribute(connEle, "indexName");
            settings.setSetting(CompassEnvironment.CONNECTION, "space://" + indexName + ":" + url);
            // we don't use the static constant so we don't create dependency on GigaSpaces
            settings.setSetting("compass.engine.store.space.bucketSize", getElementAttribute(connEle, "bucketSize"));
            settings.setSetting("compass.engine.store.space.flushRate", getElementAttribute(connEle, "flushRate"));
            return;
        }
        // --- Terracota Connection ---
        child = DomUtils.getChildElementsByTagName(ele, "tc", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String indexName = getElementAttribute(connEle, "indexName");
            settings.setSetting(CompassEnvironment.CONNECTION, "tc://" + indexName);
            settings.setSetting("compass.engine.store.tc.bufferSize", getElementAttribute(connEle, "bufferSize"));
            settings.setSetting("compass.engine.store.tc.type", getElementAttribute(connEle, "type"));
            settings.setSetting("compass.engine.store.tc.flushRate", getElementAttribute(connEle, "flushRate"));
            settings.setSetting("compass.engine.store.tc.chm.initialCapacity", getElementAttribute(connEle, "chmInitialCapacity"));
            settings.setSetting("compass.engine.store.tc.chm.loadFactor", getElementAttribute(connEle, "chmLoadFactor"));
            settings.setSetting("compass.engine.store.tc.chm.concurrencyLevel", getElementAttribute(connEle, "chmConcurrencyLevel"));
            return;
        }
        // --- Coherence Connection ---
        child = DomUtils.getChildElementsByTagName(ele, "coherence", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String indexName = getElementAttribute(connEle, "indexName");
            String cacheName = getElementAttribute(connEle, "cacheName");
            String type = getElementAttribute(connEle, "type", "invocable");
            if ("invocable".equals(type)) {
                settings.setSetting(CompassEnvironment.CONNECTION, "coherence://" + indexName + ":" + cacheName);
            } else {
                settings.setSetting(CompassEnvironment.CONNECTION, "coherence-dg://" + indexName + ":" + cacheName);
            }
            // we don't use the static constant so we don't create dependency on GigaSpaces
            settings.setSetting("compass.engine.store.coherence.bucketSize", getElementAttribute(connEle, "bucketSize"));
            settings.setSetting("compass.engine.store.coherence.flushRate", getElementAttribute(connEle, "flushRate"));
            return;
        }
        // --- Custom Connection ---
        child = DomUtils.getChildElementsByTagName(ele, "custom", true);
        if (child.size() == 1) {
            Element connEle = (Element) child.get(0);
            String url = getElementAttribute(connEle, "url");
            settings.setSetting(CompassEnvironment.CONNECTION, url);
            return;
        }
        // --- JDBC Connection --
        child = DomUtils.getChildElementsByTagName(ele, "jdbc", true);
        Element connEle = (Element) child.get(0);
        // managed
        settings.setSetting(LuceneEnvironment.JdbcStore.MANAGED, getElementAttribute(connEle, "managed", "false"));
        // disable schema operations
        settings.setSetting(LuceneEnvironment.JdbcStore.DISABLE_SCHEMA_OPERATIONS, getElementAttribute(connEle, "disableSchemaOperations", "false"));
        // dialect
        settings.setSetting(LuceneEnvironment.JdbcStore.DIALECT, getElementAttribute(connEle, "dialect"));
        settings.setSetting(LuceneEnvironment.JdbcStore.DIALECT, getElementAttribute(connEle, "dialectClass"));
        // delete mark deleted
        settings.setSetting(LuceneEnvironment.JdbcStore.DELETE_MARK_DELETED_DELTA, getElementAttribute(connEle, "deleteMarkDeletedDelta"));
        // lock
        settings.setSetting(LuceneEnvironment.JdbcStore.LOCK_TYPE, getElementAttribute(connEle, "lock"));
        settings.setSetting(LuceneEnvironment.JdbcStore.LOCK_TYPE, getElementAttribute(connEle, "lockClass"));

        // configure file entries
        child = DomUtils.getChildElementsByTagName(connEle, "fileEntries", true);
        if (child.size() == 1) {
            Element fileEntriesEle = (Element) child.get(0);
            child = DomUtils.getChildElementsByTagName(fileEntriesEle, "fileEntry", true);
            for (Iterator it = child.iterator(); it.hasNext();) {
                Element fileEntryEle = (Element) it.next();
                SettingsHolder settingsHolder = processSettings(fileEntryEle);
                // --- File Entry Index Input ---
                child = DomUtils.getChildElementsByTagName(fileEntryEle, "indexInput", true);
                if (child.size() == 1) {
                    Element indexInputEle = (Element) child.get(0);
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_INPUT_TYPE);
                    settingsHolder.values.add(getElementAttribute(indexInputEle, "type"));
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_INPUT_TYPE);
                    settingsHolder.values.add(getElementAttribute(indexInputEle, "typeClass"));
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_INPUT_BUFFER_SIZE);
                    settingsHolder.values.add(getElementAttribute(indexInputEle, "bufferSize"));
                }
                // --- File Entry Index Input ---
                child = DomUtils.getChildElementsByTagName(fileEntryEle, "indexOutput", true);
                if (child.size() == 1) {
                    Element indexOutputEle = (Element) child.get(0);
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_OUTPUT_TYPE);
                    settingsHolder.values.add(getElementAttribute(indexOutputEle, "type"));
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_OUTPUT_TYPE);
                    settingsHolder.values.add(getElementAttribute(indexOutputEle, "typeClass"));
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_OUTPUT_BUFFER_SIZE);
                    settingsHolder.values.add(getElementAttribute(indexOutputEle, "bufferSize"));
                    settingsHolder.names.add(LuceneEnvironment.JdbcStore.FileEntry.INDEX_OUTPUT_THRESHOLD);
                    settingsHolder.values.add(getElementAttribute(indexOutputEle, "threshold"));
                }
                settings.setGroupSettings(LuceneEnvironment.JdbcStore.FileEntry.PREFIX, getElementAttribute(fileEntryEle, "name"),
                        settingsHolder.names(), settingsHolder.values());
            }
        }

        // configure ddl
        child = DomUtils.getChildElementsByTagName(connEle, "ddl", true);
        if (child.size() == 1) {
            Element ddlEle = (Element) child.get(0);
            child = DomUtils.getChildElementsByTagName(ddlEle, "nameColumn", true);
            if (child.size() == 1) {
                Element nameColumnEle = (Element) child.get(0);
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.NAME_NAME, getElementAttribute(nameColumnEle, "name"));
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.NAME_LENGTH, getElementAttribute(nameColumnEle, "length"));
            }
            child = DomUtils.getChildElementsByTagName(ddlEle, "valueColumn", true);
            if (child.size() == 1) {
                Element valueColumnEle = (Element) child.get(0);
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.VALUE_NAME, getElementAttribute(valueColumnEle, "name"));
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.VALUE_LENGTH, getElementAttribute(valueColumnEle, "length"));
            }
            child = DomUtils.getChildElementsByTagName(ddlEle, "sizeColumn", true);
            if (child.size() == 1) {
                Element sizeColumnEle = (Element) child.get(0);
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.SIZE_NAME, getElementAttribute(sizeColumnEle, "name"));
            }
            child = DomUtils.getChildElementsByTagName(ddlEle, "lastModifiedColumn", true);
            if (child.size() == 1) {
                Element lastModifiedEle = (Element) child.get(0);
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.LAST_MODIFIED_NAME, getElementAttribute(lastModifiedEle, "name"));
            }
            child = DomUtils.getChildElementsByTagName(ddlEle, "deletedColumn", true);
            if (child.size() == 1) {
                Element deletedEle = (Element) child.get(0);
                settings.setSetting(LuceneEnvironment.JdbcStore.DDL.DELETED_NAME, getElementAttribute(deletedEle, "name"));
            }
        }

        // configure the data source provider
        child = DomUtils.getChildElementsByTagName(connEle, "dataSourceProvider", true);
        Element dataSourceProviderEle = (Element) child.get(0);
        // --- driverManager
        child = DomUtils.getChildElementsByTagName(dataSourceProviderEle, "driverManager", true);
        if (child.size() == 1) {
            Element driverManagerEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                    "org.compass.core.lucene.engine.store.jdbc.DriverManagerDataSourceProvider");
            settings.setSetting(CompassEnvironment.CONNECTION,
                    "jdbc://" + getElementAttribute(driverManagerEle, "url"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME, getElementAttribute(driverManagerEle, "username"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD, getElementAttribute(driverManagerEle, "password"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, getElementAttribute(driverManagerEle, "driverClass"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, getElementAttribute(driverManagerEle, "autoCommit", "false"));
        }
        child = DomUtils.getChildElementsByTagName(dataSourceProviderEle, "c3p0", true);
        if (child.size() == 1) {
            Element driverManagerEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                    "org.compass.core.lucene.engine.store.jdbc.C3P0DataSourceProvider");
            settings.setSetting(CompassEnvironment.CONNECTION, "jdbc://" + getElementAttribute(driverManagerEle, "url"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME, getElementAttribute(driverManagerEle, "username"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD, getElementAttribute(driverManagerEle, "password"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, getElementAttribute(driverManagerEle, "driverClass"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, getElementAttribute(driverManagerEle, "autoCommit", "false"));
        }
        child = DomUtils.getChildElementsByTagName(dataSourceProviderEle, "jndi", true);
        if (child.size() == 1) {
            Element jndiEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                    "org.compass.core.lucene.engine.store.jdbc.JndiDataSourceProvider");
            settings.setSetting(CompassEnvironment.CONNECTION, "jdbc://" + getElementAttribute(jndiEle, "lookup"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME, getElementAttribute(jndiEle, "username"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD, getElementAttribute(jndiEle, "password"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, getElementAttribute(jndiEle, "autoCommit", "false"));
        }
        child = DomUtils.getChildElementsByTagName(dataSourceProviderEle, "dbcp", true);
        if (child.size() == 1) {
            Element dbcpEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                    "org.compass.core.lucene.engine.store.jdbc.DbcpDataSourceProvider");
            settings.setSetting(CompassEnvironment.CONNECTION, "jdbc://" + getElementAttribute(dbcpEle, "url"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME, getElementAttribute(dbcpEle, "username"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD, getElementAttribute(dbcpEle, "password"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS, getElementAttribute(dbcpEle, "driverClass"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, getElementAttribute(dbcpEle, "autoCommit", "false"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.DEFAULT_TRANSACTION_ISOLATION, getElementAttribute(dbcpEle, "defaultTransacitonIsolation"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.INITIAL_SIZE, getElementAttribute(dbcpEle, "initialSize"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_ACTIVE, getElementAttribute(dbcpEle, "maxActive"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_IDLE, getElementAttribute(dbcpEle, "maxIdle"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MIN_IDLE, getElementAttribute(dbcpEle, "minIdle"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_WAIT, getElementAttribute(dbcpEle, "maxWait"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_OPEN_PREPARED_STATEMENTS, getElementAttribute(dbcpEle, "maxOpenPreparedStatements"));
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.POOL_PREPARED_STATEMENTS, getElementAttribute(dbcpEle, "poolPreparedStatements"));
        }
        child = DomUtils.getChildElementsByTagName(dataSourceProviderEle, "external", true);
        if (child.size() == 1) {
            Element externalEle = (Element) child.get(0);
            settings.setSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.CLASS,
                    "org.compass.core.lucene.engine.store.jdbc.ExternalDataSourceProvider");
            settings.setSetting(CompassEnvironment.CONNECTION, "jdbc://");
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME, getElementAttribute(externalEle, "username"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD, getElementAttribute(externalEle, "password"));
            settings.setSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, getElementAttribute(externalEle, "autoCommit", "false"));
        }
    }

    public void bindJndi(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        settings.setSetting(CompassEnvironment.Jndi.ENABLE, getElementAttribute(ele, "register", "false"));
        settings.setSetting(CompassEnvironment.Jndi.CLASS, getElementAttribute(ele, "class"));
        settings.setSetting(CompassEnvironment.Jndi.URL, getElementAttribute(ele, "url"));
        List environments = DomUtils.getChildElementsByTagName(ele, "environment", true);
        if (environments.size() == 1) {
            Element environment = (Element) environments.get(0);
            List properties = DomUtils.getChildElementsByTagName(environment, "property", true);
            for (Iterator it = properties.iterator(); it.hasNext();) {
                Element property = (Element) it.next();
                String propertyName = CompassEnvironment.Jndi.PREFIX + "." + getElementAttribute(property, "name");
                String propertyValue = getElementAttribute(property, "value");
                settings.setSetting(propertyName, propertyValue);
            }
        }
    }

    public void bindSettings(Element ele, CompassConfiguration config) {
        CompassSettings settings = config.getSettings();
        List domSettings = DomUtils.getChildElementsByTagName(ele, "setting", true);
        for (Iterator it = domSettings.iterator(); it.hasNext();) {
            Element eleSetting = (Element) it.next();
            settings.setSetting(getElementAttribute(eleSetting, "name"),
                    getElementAttribute(eleSetting, "value"));
        }
    }

    public void bindMappings(Element ele, CompassConfiguration config) throws Exception {
        NodeList nl = ele.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element mappingEle = (Element) node;
                String nodeName = mappingEle.getLocalName();
                if ("resource".equals(nodeName)) {
                    config.addResource(getElementAttribute(mappingEle, "location"));
                } else if ("class".equals(nodeName)) {
                    config.addClass(ClassUtils.forName(getElementAttribute(mappingEle, "name"), config.getClassLoader()));
                } else if ("jar".equals(nodeName)) {
                    config.addJar(new File(getElementAttribute(mappingEle, "path")));
                } else if ("file".equals(nodeName)) {
                    config.addFile(new File(getElementAttribute(mappingEle, "path")));
                } else if ("dir".equals(nodeName)) {
                    config.addDirectory(new File(getElementAttribute(mappingEle, "path")));
                } else if ("package".equals(nodeName)) {
                    config.addPackage(getElementAttribute(mappingEle, "name"));
                } else if ("scan".equals(nodeName)) {
                    config.addScan(getElementAttribute(mappingEle, "basePackage"), getElementAttribute(mappingEle, "pattern"));
                }
            }
        }
    }

    private SettingsHolder processSettings(Element ele) {
        SettingsHolder settingsHolder = new SettingsHolder();
        List settings = DomUtils.getChildElementsByTagName(ele, "setting", true);
        for (Iterator it = settings.iterator(); it.hasNext();) {
            Element settingEle = (Element) it.next();
            settingsHolder.names.add(getElementAttribute(settingEle, "name"));
            settingsHolder.values.add(getElementAttribute(settingEle, "value"));
        }
        return settingsHolder;
    }

    private class SettingsHolder {
        public ArrayList names = new ArrayList();
        public ArrayList values = new ArrayList();

        public String[] names() {
            return (String[]) names.toArray(new String[names.size()]);
        }

        public String[] values() {
            return (String[]) values.toArray(new String[values.size()]);
        }
    }

    protected EntityResolver doGetEntityResolver() {
        return new EntityResolver() {

            private static final String URL = "http://www.compass-project.org/schema/";

            public InputSource resolveEntity(String publicId, String systemId) {
                if (systemId != null && systemId.startsWith("http://www.opensymphony.com/compass/schema/")) {
                    throw new IllegalArgumentException("Using old format for schema, please use the url [" + URL + "]");
                }
                if (systemId != null && systemId.startsWith(URL)) {
                    // Search for DTD
                    String location = "/org/compass/core/" + systemId.substring(URL.length());
                    InputStream is = getClass().getResourceAsStream(location);
                    if (is == null) {
                        throw new ConfigurationException("Schema system id [" + systemId + "] not found at [" + location + "], " +
                                "please check it has the correct location. Have you included compass in your class path?");
                    }
                    InputSource source = new InputSource(is);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                } else {
                    throw new ConfigurationException("Schema system id [" + systemId + "] not found, please check it has the " +
                            "correct location");
                }
            }
        };
    }

    protected DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = super.createDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        try {
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException(
                    "Unable to validate using XSD: Your JAXP provider [" + factory + "] does not support XML Schema. "
                            + "Are you running on Java 1.4 or below with Apache Crimson? "
                            + "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
        }
        return factory;
    }
}
