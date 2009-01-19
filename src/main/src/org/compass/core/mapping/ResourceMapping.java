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

package org.compass.core.mapping;

import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.subindex.SubIndexHash;

/**
 * A mapping defining a {@link org.compass.core.Resource} saved in the Search Engine.
 *
 * @author kimchy
 */
public interface ResourceMapping extends AliasMapping {

    /**
     * The sub index that the {@link org.compass.core.Resource} will be saved to in the
     * search engine.
     */
    SubIndexHash getSubIndexHash();

    /**
     * Returns the analyzer name that need to be used with the resouce.
     * Can be <code>null</code>.
     */
    String getAnalyzer();

    /**
     * Returns <code>true</code> if the resource proeprty mapping of the resource have
     * a specific analyzer set.
     */
    boolean hasSpecificAnalyzerPerResourceProperty();

    /**
     * Returns the analzer controller that is associated with the {@link org.compass.core.Resource}.
     * Can be <code>null</code>.
     */
    ResourceAnalyzerController getAnalyzerController();

    /**
     * Returns an optional boost property mapping associated with the {@link org.compass.core.Resource}
     * in order to dynmaically set the resource boost value based on it. Can be <code>null</code>.
     */
    BoostPropertyMapping getBoostPropertyMapping();

    /**
     * Returns the boost of the given resource.
     */
    float getBoost();

    /**
     * Returns <code>true</code> if it is a root resource mapping. If it is a
     * root mapping, then it controlls the general {@link org.compass.core.Resource} level
     * settings like alias and sub-index. Usually, non-root resource mapping are mappings that are
     * incorporated within another resoruce.
     */
    boolean isRoot();

    /**
     * Returns a set of settings associated with the all mappings.
     */
    AllMapping getAllMapping();

    /**
     * Returns all mappings that represent ids of a resource.
     */
    Mapping[] getIdMappings();

    /**
     * Returns the default spell check mode. If NA, will use global setting.
     */
    SpellCheck getSpellCheck();

    /**
     * Returns the uid property name.
     */
    String getUIDPath();

    /**
     * Returns all the id mappigns for the low level resource mapping.
     */
    ResourcePropertyMapping[] getResourceIdMappings();

    /**
     * Returns a list of all the cascade mappings. Will return null if no cascading is defined.
     */
    CascadeMapping[] getCascadeMappings();

    /**
     * Returns is an operation is allowed to be performed on this resoruce mappings.
     * Root mappings ({@link #isRoot()} always return <code>true</code>, while if
     * it is non root mappings, it should return true if it has cascade mappings
     * that map one of the cascade operations.
     */
    boolean operationAllowed(Cascade cascade);

    /**
     * Returns all the mapped property names for the resoruce.
     */
    String[] getResourcePropertyNames();

    /**
     * Returns all the resource property mappings that are assoicated with the resource mapping.
     * Note, that it is an important method, since it might be that not all the mappings that were
     * added to the resource mapping are of type {@link ResourcePropertyMapping}, and using this
     * method you can be assured that ALL the {@link ResourcePropertyMapping}s will be returned
     * (even deep ones).
     */
    ResourcePropertyMapping[] getResourcePropertyMappings();

    /**
     * Returns the first resource property mapping that match the given proeprty name
     * ({@link org.compass.core.mapping.ResourcePropertyMapping#getName()}, or
     * <code>null</code> if not exists.
     */
    ResourcePropertyMapping getResourcePropertyMapping(String propertyName);

    /**
     * Returns the list of resource property mappings that match the given proeprty name
     * ({@link org.compass.core.mapping.ResourcePropertyMapping#getName()}, or
     * <code>null</code> if not exists.
     */
    ResourcePropertyMapping[] getResourcePropertyMappings(String propertyName);

    /**
     * Returns the {@link ResourcePropertyMapping} that match the given path
     * ({@link org.compass.core.mapping.ResourcePropertyMapping#getPath()}, or
     * <code>null</code> of not exists.
     */
    ResourcePropertyMapping getResourcePropertyMappingByPath(PropertyPath path);

    /**
     * Returns the {@link ResourcePropertyMapping} that match the given path
     * "dot" path (a.bValue.value), or <code>null</code> if none exists.
     */
    ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path);
}
