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

package org.compass.gps.device.jpa.entities;

/**
 * A general Entity information to be used by the {@link org.compass.gps.device.jpa.JpaGpsDevice}
 * during the indexing process.
 *
 * @author kimchy
 * @see JpaEntitiesLocator
 */
public class EntityInformation {

    private Class<?> clazz;

    private String name;

    private String selectQuery;

    public EntityInformation(Class<?> clazz, String name) {
        this(clazz, name, "select x from " + name + " x");
    }

    public EntityInformation(Class<?> clazz, String name, String selectQuery) {
        this.clazz = clazz;
        this.name = name;
        this.selectQuery = selectQuery;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getSelectQuery() {
        return selectQuery;
    }
}
