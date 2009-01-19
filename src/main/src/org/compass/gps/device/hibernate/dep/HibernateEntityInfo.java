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

package org.compass.gps.device.hibernate.dep;

import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * A data holder used to index a specific Hibernate entity.
 */
public class HibernateEntityInfo implements IndexEntity {

    private String entityName;

    private String selectQuery;

    private String[] subIndexes;

    public HibernateEntityInfo(String entityname, String selectQuery, String[] subIndexes) {
        this.entityName = entityname;
        this.selectQuery = selectQuery;
        this.subIndexes = subIndexes;
    }

    public String getName() {
        return entityName;
    }

    public String getSelectQuery() {
        return this.selectQuery;
    }

    public String[] getSubIndexes() {
        return subIndexes;
    }
}
