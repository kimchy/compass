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

package org.compass.gps;

/**
 * Index plan is a general class representing what needs to be indexed. By default,
 * everything (types, aliases, and sub indexes) are set to <code>null</code>.
 *
 * <p>If more than one setter is used (i.e. has a non null value), then the join
 * of all of the different setter values that are not null will be used to index.
 *
 * <p>Note, in case of inheritance (for alias or class), the extending classes will
 * also be indexed.
 *
 * @author kimchy
 */
public interface IndexPlan {

    /**
     * Sets the given classes that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    IndexPlan setTypes(Class... types);

    /**
     * Returns the given classes that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    Class[] getTypes();

    /**
     * Sets the given aliases that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    IndexPlan setAliases(String... aliases);

    /**
     * Returns the given aliases that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    String[] getAliases();

    /**
     * Sets the given sub indexes that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    IndexPlan setSubIndexes(String... subIndexes);

    /**
     * Returns the given sub indexes that will be indexed. <code>null</code> value
     * means that it will not be taken into account.
     */
    String[] getSubIndexes();
}
