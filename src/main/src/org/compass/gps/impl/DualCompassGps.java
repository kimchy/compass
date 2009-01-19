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

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassTemplate;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;

/**
 * <p>A {@link org.compass.gps.CompassGps} implementation that holds two
 * <code>Compass</code> instances. One, called <code>indexCompass</code> is
 * responsible for index operation. The other, called <code>mirrorCompass</code>
 * is responsible for mirror operations.
 *
 * <p>The index compass settings should probably work in batch insert mode and
 * disable cascading.
 *
 * @author kimchy
 * @deprecated Please use {@link org.compass.gps.impl.SingleCompassGps}.
 */
public class DualCompassGps extends AbstractCompassGps {

    private Compass indexCompass;

    private CompassTemplate indexCompassTemplate;

    private Compass mirrorCompass;

    private CompassTemplate mirrorCompassTemplate;

    public DualCompassGps() {

    }

    public DualCompassGps(Compass indexCompass, Compass mirrorCompass) {
        this.indexCompass = indexCompass;
        this.mirrorCompass = mirrorCompass;
    }

    protected void doStart() throws CompassGpsException {
        if (indexCompass == null) {
            throw new IllegalArgumentException("Must set the indexCompass property (for batch indexing)");
        }
        this.indexCompassTemplate = new CompassTemplate(indexCompass);
        if (mirrorCompass != null) {
            this.mirrorCompassTemplate = new CompassTemplate(mirrorCompass);
        }
    }

    protected void doStop() throws CompassGpsException {
    }

    protected void doIndex(final IndexPlan indexPlan) throws CompassGpsException {
        if (mirrorCompass != null) {
            ((InternalCompass) mirrorCompass).stop();
        }
        ((InternalCompass) indexCompass).stop();

        indexCompass.getSearchEngineIndexManager().clearCache();
        indexCompass.getSearchEngineIndexManager().deleteIndex();
        indexCompass.getSearchEngineIndexManager().createIndex();

        mirrorCompass.getSearchEngineIndexManager().replaceIndex(
                indexCompass.getSearchEngineIndexManager(), new DefaultReplaceIndexCallback(devices.values(), indexPlan));
        if (mirrorCompass != null) {
            ((InternalCompass) mirrorCompass).start();
        }
        ((InternalCompass) indexCompass).start();

        if (mirrorCompass.getSpellCheckManager() != null) {
            log.info("Rebulding spell check index ...");
            try {
                mirrorCompass.getSpellCheckManager().concurrentRebuild();
                log.info("Spell check index rebuilt");
            } catch (Exception e) {
                log.info("Spell check index failed, will rebuilt it next time", e);
            }
        }

        // add a rebuild listener
        ((InternalCompass) indexCompass).addRebuildEventListener(new RebuildEventListener() {
            public void onCompassRebuild(Compass compass) {
                if (log.isDebugEnabled()) {
                    log.debug("Rebuild detected, restarting");
                }
                refresh();
            }
        });
        ((InternalCompass) mirrorCompass).addRebuildEventListener(new RebuildEventListener() {
            public void onCompassRebuild(Compass compass) {
                if (log.isDebugEnabled()) {
                    log.debug("Rebuild detected, restarting");
                }
                refresh();
            }
        });
    }

    public void executeForIndex(CompassCallback callback) throws CompassException {
        indexCompassTemplate.execute(callback);
    }

    public void executeForMirror(CompassCallback callback) throws CompassException {
        mirrorCompassTemplate.execute(callback);
    }

    public boolean hasMappingForEntityForIndex(Class clazz) throws CompassException {
        return hasRootMappingForEntity(clazz, indexCompass);
    }

    public boolean hasMappingForEntityForIndex(String name) throws CompassException {
        return hasRootMappingForEntity(name, indexCompass);
    }

    public boolean hasMappingForEntityForMirror(Class clazz, Cascade cascade) throws CompassException {
        return hasMappingForEntity(clazz, mirrorCompass, cascade);
    }

    public boolean hasMappingForEntityForMirror(String name, Cascade cascade) throws CompassException {
        return hasMappingForEntity(name, mirrorCompass, cascade);
    }

    public ResourceMapping getMappingForEntityForIndex(String name) throws CompassException {
        return getRootMappingForEntity(name, indexCompass);
    }

    public ResourceMapping getMappingForEntityForIndex(Class clazz) throws CompassException {
        return getRootMappingForEntity(clazz, indexCompass);
    }

    public Compass getIndexCompass() {
        return indexCompass;
    }

    /**
     * Sets the index compass instance, used for the indexing operations.
     */
    public void setIndexCompass(Compass indexCompass) {
        this.indexCompass = indexCompass;
    }

    public Compass getMirrorCompass() {
        return mirrorCompass;
    }

    /**
     * Sets the mirror compass instance, used for the mirroring operations.
     */
    public void setMirrorCompass(Compass mirrorCompass) {
        this.mirrorCompass = mirrorCompass;
    }

    public CompassTemplate getIndexCompassTemplate() {
        return indexCompassTemplate;
    }

    public CompassTemplate getMirrorCompassTemplate() {
        return mirrorCompassTemplate;
    }

}
