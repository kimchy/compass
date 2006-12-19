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

package org.compass.gps.device.support.parallel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.compass.gps.CompassGpsException;

/**
 * Partitions a list of {@link org.compass.gps.device.support.parallel.IndexEntity} into
 * several groups of lists of index entities based on their sub indexes. Index entities
 * that share the same sub index will be grouped into the same group since parallel indexing
 * is best performed based on the sub index as Compass perfoms sub index level locking during
 * indexing.
 *
 * @author kimchy
 */
public class SubIndexIndexEntitiesPartitioner implements IndexEntitiesPartitioner {

    public IndexEntity[][] partition(IndexEntity[] entities) throws CompassGpsException {
        LinkedList list = new LinkedList();
        for (int i = 0; i < entities.length; i++) {
            Holder holder = new Holder();
            IndexEntity indexEntity = entities[i];
            holder.indexEntities.add(indexEntity);
            for (int j = 0; j < indexEntity.getSubIndexes().length; j++) {
                holder.subIndexes.add(indexEntity.getSubIndexes()[j]);
            }
            list.add(holder);
        }
        boolean merged;
        do {
            merged = false;
            for (int i = 0; i < list.size(); i++) {
                Holder holder = (Holder) list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    Holder tempHolder = (Holder) list.get(j);
                    if (holder.containsSubIndex(tempHolder.subIndexes)) {
                        list.remove(j);
                        holder.subIndexes.addAll(tempHolder.subIndexes);
                        holder.indexEntities.addAll(tempHolder.indexEntities);
                        merged = true;
                    }
                }
            }
        } while (merged);
        IndexEntity[][] returnEntities = new IndexEntity[list.size()][];
        for (int i = 0; i < returnEntities.length; i++) {
            Holder holder = (Holder) list.get(i);
            returnEntities[i] = (IndexEntity[]) holder.indexEntities.toArray(new IndexEntity[holder.indexEntities.size()]);
        }
        return returnEntities;
    }

    private class Holder {
        Set subIndexes = new HashSet();
        Set indexEntities = new HashSet();

        public boolean containsSubIndex(Set checkSubIndexes) {
            for (Iterator it = checkSubIndexes.iterator(); it.hasNext();) {
                if (subIndexes.contains(it.next())) {
                    return true;
                }
            }
            return false;
        }
    }
}
