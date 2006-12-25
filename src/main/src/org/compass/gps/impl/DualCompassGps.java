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

package org.compass.gps.impl;

import java.util.Iterator;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassTemplate;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.mapping.ResourceMapping;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.CompassGpsException;

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

    protected void doIndex() throws CompassGpsException {
        boolean stoppedMirrorCompassOptimizer = false;
        boolean stoppedIndexCompassOptimizer = false;
        boolean stoppedIndexCompassIndexManager = false;
        if (mirrorCompass != null && mirrorCompass.getSearchEngineOptimizer().isRunning()) {
            mirrorCompass.getSearchEngineOptimizer().stop();
            stoppedMirrorCompassOptimizer = true;
        }
        if (indexCompass.getSearchEngineOptimizer().isRunning()) {
            indexCompass.getSearchEngineOptimizer().stop();
            stoppedIndexCompassOptimizer = true;
        }
        if (indexCompass.getSearchEngineIndexManager().isRunning()) {
            indexCompass.getSearchEngineIndexManager().stop();
            stoppedIndexCompassIndexManager = true;
        }

        indexCompass.getSearchEngineIndexManager().clearCache();
        indexCompass.getSearchEngineIndexManager().deleteIndex();
        indexCompass.getSearchEngineIndexManager().createIndex();

        mirrorCompass.getSearchEngineIndexManager().replaceIndex(
                indexCompass.getSearchEngineIndexManager(), new SearchEngineIndexManager.ReplaceIndexCallback() {
            public void buildIndexIfNeeded() throws SearchEngineException {
                for (Iterator it = devices.values().iterator(); it.hasNext();) {
                    CompassGpsDevice device = (CompassGpsDevice) it.next();
                    device.index();
                }
            }
        });

        if (stoppedMirrorCompassOptimizer) {
            mirrorCompass.getSearchEngineOptimizer().start();
        }
        if (stoppedIndexCompassOptimizer) {
            indexCompass.getSearchEngineOptimizer().start();
        }
        if (stoppedIndexCompassIndexManager) {
            indexCompass.getSearchEngineIndexManager().start();
        }
    }

    public void executeForIndex(CompassCallback callback) throws CompassException {
        indexCompassTemplate.execute(callback);
    }

    public void executeForMirror(CompassCallback callback) throws CompassException {
        mirrorCompassTemplate.execute(callback);
    }

    public boolean hasMappingForEntityForIndex(Class clazz) throws CompassException {
        return hasMappingForEntity(clazz, indexCompass);
    }

    public boolean hasMappingForEntityForIndex(String name) throws CompassException {
        return hasMappingForEntity(name, indexCompass);
    }

    public boolean hasMappingForEntityForMirror(Class clazz) throws CompassException {
        return hasMappingForEntity(clazz, mirrorCompass);
    }

    public boolean hasMappingForEntityForMirror(String name) throws CompassException {
        return hasMappingForEntity(name, mirrorCompass);
    }

    public ResourceMapping getMappingForEntityForIndex(String name) throws CompassException {
        return getMappingForEntity(name, indexCompass);
    }

    public ResourceMapping getMappingForEntityForIndex(Class clazz) throws CompassException {
        return getMappingForEntity(clazz, indexCompass);
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
