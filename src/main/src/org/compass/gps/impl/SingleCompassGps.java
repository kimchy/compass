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

package org.compass.gps.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;

/**
 * <p>A {@link org.compass.gps.CompassGps} implementation that holds a
 * single <code>Compass</code> instance. The <code>Compass</code> instance
 * is used for both the index operation and the mirror operation.
 *
 * @author kimchy
 */
public class SingleCompassGps extends AbstractCompassGps {

    private Compass compass;

    private CompassTemplate compassTemplate;

    private volatile Compass indexCompass;

    private volatile CompassTemplate indexCompassTemplate;

    private Map<String, Object> indexSettings;

    private CompassSettings indexCompassSettings;

    public SingleCompassGps() {

    }

    public SingleCompassGps(Compass compass) {
        this.compass = compass;
    }

    protected void doStart() throws CompassGpsException {
        if (compass == null) {
            throw new IllegalArgumentException("Must set the compass property");
        }
        indexCompassSettings = new CompassSettings();
        if (indexSettings != null) {
            indexCompassSettings.addSettings(indexSettings);
        }
        if (indexCompassSettings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT) == null) {
            indexCompassSettings.setSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT, "gpsindex");
        }
        if (indexCompassSettings.getSetting(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE) == null) {
            indexCompassSettings.setBooleanSetting(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE, true);
        }
        // indexing relies on thread local binding of local transactions
        if (indexCompassSettings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
            indexCompassSettings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, false);
        }
        if (indexCompassSettings.getSetting(CompassEnvironment.Cascade.DISABLE) == null) {
            indexCompassSettings.setBooleanSetting(CompassEnvironment.Cascade.DISABLE, true);
        }
        if (indexCompassSettings.getSetting(LuceneEnvironment.Transaction.Processor.TYPE) == null) {
            indexCompassSettings.setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        }
        // no need to async load the cache
        indexCompassSettings.setBooleanSetting(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, false);
        indexCompassSettings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_AUTO_JOIN_SESSION, true);
        this.compassTemplate = new CompassTemplate(compass);

        // add a rebuild listener
        ((InternalCompass) compass).addRebuildEventListener(new RebuildEventListener() {
            public void onCompassRebuild(Compass compass) {
                if (log.isDebugEnabled()) {
                    log.debug("Rebuild detected, restarting");
                }
                refresh();
            }
        });
    }

    protected void doStop() throws CompassGpsException {
    }

    protected void doIndex(final IndexPlan indexPlan) throws CompassGpsException {
        ((InternalCompass) compass).stop();

        // create the temp compass index, and clean it
        indexCompass = compass.clone(indexCompassSettings);
        indexCompass.getSearchEngineIndexManager().cleanIndex();
        indexCompassTemplate = new CompassTemplate(indexCompass);

        indexCompass.getSearchEngineIndexManager().clearCache();
        compass.getSearchEngineIndexManager().replaceIndex(indexCompass.getSearchEngineIndexManager(),
                new DefaultReplaceIndexCallback(devices.values(), indexPlan));
        indexCompass.getSearchEngineIndexManager().clearCache();
        try {
            indexCompass.getSearchEngineIndexManager().deleteIndex();
        } catch (CompassException e) {
            log.debug("Failed to delete gps index after indexing, ignoring", e);
        }
        indexCompass.close();
        indexCompass = null;
        indexCompassTemplate = null;

        ((InternalCompass) compass).start();

        if (compass.getSpellCheckManager() != null) {
            log.info("Rebulding spell check index ...");
            try {
                compass.getSpellCheckManager().concurrentRebuild();
                log.info("Spell check index rebuilt");
            } catch (Exception e) {
                log.info("Spell check index failed, will rebuilt it next time", e);
            }
        }
    }

    public void executeForIndex(CompassCallback callback) throws CompassException {
        if (indexCompassTemplate == null) {
            throw new IllegalStateException("executeForIndex is called outside of an index operation");
        }
        indexCompassTemplate.execute(callback);
    }

    public void executeForMirror(CompassCallback callback) throws CompassException {
        compassTemplate.execute(callback);
    }

    public boolean hasMappingForEntityForIndex(Class clazz) throws CompassException {
        return hasRootMappingForEntity(clazz, getIndexCompass());
    }

    public boolean hasMappingForEntityForIndex(String name) throws CompassException {
        return hasRootMappingForEntity(name, getIndexCompass());
    }

    public boolean hasMappingForEntityForMirror(Class clazz, Cascade cascade) throws CompassException {
        return hasMappingForEntity(clazz, compass, cascade);
    }

    public boolean hasMappingForEntityForMirror(String name, Cascade cascade) throws CompassException {
        return hasMappingForEntity(name, compass, cascade);
    }

    public ResourceMapping getMappingForEntityForIndex(String name) throws CompassException {
        return getRootMappingForEntity(name, getIndexCompass());
    }

    public ResourceMapping getMappingForEntityForIndex(Class clazz) throws CompassException {
        return getRootMappingForEntity(clazz, getIndexCompass());
    }

    public Compass getIndexCompass() {
        if (indexCompass == null) {
            return compass;
        }
        return indexCompass;
    }

    public Compass getMirrorCompass() {
        return compass;
    }

    /**
     * Sets the compass instance that will be used with this Gps implementation.
     * It will be used directly for mirror operations, and will be cloned
     * (optionally adding the {@link #setIndexSettings(java.util.Properties)}
     * for index operations.
     */
    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(Properties indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        for (Map.Entry entry : indexSettings.entrySet()) {
            this.indexSettings.put((String) entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(Map<String, Object> indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        this.indexSettings.putAll(indexSettings);
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexProperties(Properties indexSettings) {
        setIndexSettings(indexSettings);
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(CompassSettings indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        this.indexSettings.putAll(indexSettings.getUnderlyingMap());
    }
}
