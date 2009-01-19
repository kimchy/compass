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

package org.compass.gps.device.indexplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.device.support.parallel.AbstractParallelGpsDevice;
import org.compass.gps.device.support.parallel.IndexEntitiesIndexer;
import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * @author kimchy
 */
public class IndexPlanGpsDevice extends AbstractParallelGpsDevice {

    private Map<String, List<Object>> objects = new HashMap<String, List<Object>>();

    public IndexPlanGpsDevice() {
        objects.put("a", new ArrayList<Object>());
        objects.put("b", new ArrayList<Object>());
        objects.put("c", new ArrayList<Object>());
        objects.put("d", new ArrayList<Object>());
    }

    protected IndexEntity[] doGetIndexEntities() throws CompassGpsException {
        return new IndexEntity[]{
                new ValueIndexEntity("a", "a"),
                new ValueIndexEntity("b", "common"),
                new ValueIndexEntity("c", "common"),
                new ValueIndexEntity("d", "d_1", "d_2")
        };
    }

    protected IndexEntitiesIndexer doGetIndexEntitiesIndexer() {
        return new IndexEntitiesIndexer() {
            public void performIndex(CompassSession session, IndexEntity[] entities) throws CompassException {
                for (IndexEntity indexEntity : entities) {
                    for (Object o : objects.get(indexEntity.getName())) {
                        session.create(o);
                    }
                }
            }
        };
    }

    public void add(String name, Object value) {
        objects.get(name).add(value);
    }

    public void clear(String name) {
        objects.get(name).clear();
    }

    public void clear() {
        for (List<Object> list : objects.values()) {
            list.clear();
        }
    }

    private class ValueIndexEntity implements IndexEntity {

        private String name;

        private String[] subIndexes;

        private ValueIndexEntity(String name, String... subIndexes) {
            this.name = name;
            this.subIndexes = subIndexes;
        }

        public String getName() {
            return this.name;
        }

        public String[] getSubIndexes() {
            return this.subIndexes;
        }
    }
}
