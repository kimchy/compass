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

import java.util.Collection;

import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.gps.CompassGpsDevice;
import org.compass.gps.IndexPlan;

/**
 * @author kimchy
 */
public class DefaultReplaceIndexCallback implements SearchEngineIndexManager.ReplaceIndexCallback, SearchEngineIndexManager.IndexOperationPlan {

    private Collection<CompassGpsDevice> devices;

    private IndexPlan indexPlan;

    public DefaultReplaceIndexCallback(Collection<CompassGpsDevice> devices, IndexPlan indexPlan) {
        this.devices = devices;
        this.indexPlan = indexPlan;
    }

    public void buildIndexIfNeeded() throws SearchEngineException {
        for (CompassGpsDevice device : devices) {
            device.index(indexPlan);
        }
    }

    public String[] getSubIndexes() {
        return indexPlan.getSubIndexes();
    }

    public String[] getAliases() {
        return indexPlan.getAliases();
    }

    public Class[] getTypes() {
        return indexPlan.getTypes();
    }
}
