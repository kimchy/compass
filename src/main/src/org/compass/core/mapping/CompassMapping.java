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

import java.util.List;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyPath;

/**
 * Holds the different mapping definitions Compass has.
 *
 * @author kimchy
 */
public interface CompassMapping {

    /**
     * Returns the converter lookup.
     */
    ConverterLookup getConverterLookup();

    /**
     * Returns the root path of the mappings.
     */
    PropertyPath getPath();

    /**
     * Returns all the current mappings.
     */
    AliasMapping[] getMappings();

    /**
     * Returns all the root resource mappings.
     */
    ResourceMapping[] getRootMappings();

    /**
     * Returns the alias mapping for the given alias (most if not all of the times, this will
     * be a {@link org.compass.core.mapping.ResourceMapping}).
     */
    AliasMapping getAliasMapping(String alias);

    /**
     * Returns the resource mapping for the given alias.
     */
    ResourceMapping getMappingByAlias(String alias);

    /**
     * Returns the root resource mapping associated with the alias. Retruns
     * <code>null</code> if no root mapping (or no mapping) is associated with the alias.
     */
    ResourceMapping getRootMappingByAlias(String alias);

    /**
     * Returns the non root resource mapping associated with the alias. Retruns
     * <code>null</code> if no non root mapping (or no mapping) is associated with the alias.
     */
    ResourceMapping getNonRootMappingByAlias(String alias);

    /**
     * Returns <code>true</code> if the given alias has a root resource mapping.
     */
    boolean hasRootMappingByAlias(String alias);

    /**
     * Returns <code>true</code> if there is a <b>root</b> {@link org.compass.core.mapping.osem.ClassMapping}
     * for the given alias.
     */
    boolean hasRootClassMapping(String alias);

    /**
     * Returns <code>true</code> if there is a <b>root</b> {@link org.compass.core.mapping.rsem.RawResourceMapping}
     * for the given alias.
     */
    boolean hasRootRawResourceMapping(String alias);

    /**
     * Returns <code>true</code> if the given <b>className</b> has multiple class mappings.
     */
    boolean hasMultipleRootClassMapping(String className);

    /**
     * Returns the direct class mapping for the given class (root or not). Will not try to
     * navigate up the interface/superclass in order to find the "nearset" class mapping.
     *
     * <p>If a class has more than one mappings (using differnet aliases) will return the
     * first one.
     */
    ResourceMapping getDirectMappingByClass(Class clazz);

    /**
     * Returns all the direct class mapping for the given class (root or not). Will not
     * try to navigate up the interface/superclass in order to find the "nearest" class
     * mapping.
     */
    List<ResourceMapping> getAllDirectMappingByClass(Class clazz);

    /**
     * Finds the Resource mapping that is the "nearest" to the provided class.
     * Similar way that {@link #findRootMappingByClass(Class)} except the search
     * is on all the ClassMappings (even ones that are not marked as root).
     */
    ResourceMapping getMappingByClass(Class clazz);

    /**
     * Returns <code>true</code> if the given class has either root mappings, or if it has
     * a non root mapping, it has mappings for the given cascade operation.
     */
    boolean hasMappingForClass(Class clazz, Cascade cascade);

    /**
     * Returns <code>true</code> if the given alias has either root mappings, or if it has
     * a non root mapping, it has mappings for the given cascade operation. 
     */
    boolean hasMappingForAlias(String alias, Cascade cascade);

    /**
     * Finds a root mapping by the class name. If a root mapping is not found
     * for the class name, than searches for mappings for the interfaces, if not
     * found, checks for subclasses, and subclassess interfaces. Note: If there
     * is no direct mapping that match the class name, then the mapping that is
     * found should be marked as poly.
     *
     * @param clazz The class to find root mapping for
     * @return The resource mapping
     */
    ResourceMapping findRootMappingByClass(Class clazz) throws MappingException;

    /**
     * Does exactly the same as {@link #findRootMappingByClass(Class)}, but returns <code>null</code>
     * if nothing is found (does not throw an exception).
     */
    ResourceMapping getRootMappingByClass(Class clazz) throws MappingException;

    /**
     * Finds a non root mapping by the class name. If a non root mapping is not found
     * for the class name, than searches for mappings for the interfaces, if not
     * found, checks for subclasses, and subclassess interfaces. Note: If there
     * is no direct mapping that match the class name, then the mapping that is
     * found should be marked as poly.
     *
     * @param clazz The class to find root mapping for
     * @return The resource mapping
     */
    ResourceMapping findNonRootMappingByClass(Class clazz) throws MappingException;

    /**
     * Does exactly the same as {@link #findNonRootMappingByClass(Class)}, but returns <code>null</code>
     * if nothing is found (does not throw an exception).
     */
    ResourceMapping getNonRootMappingByClass(Class clazz) throws MappingException;

    /**
     * Returns a resoruce lookup for a specific name. Supports dot path notation ([alias].[class property].).
     * Allows to get the meta-data/resource property mapping through it (or a list of mappings).
     */
    ResourcePropertyLookup getResourcePropertyLookup(String name) throws IllegalArgumentException;

    /**
     * Finds the {@link ResourcePropertyMapping} definition for the specified path. The
     * path is in the format of: [alias].[class property mapping].[meta data mapping] in
     * case of class mapping, and [alias].[resource property mapping] in case of resource
     * mapping. The format of [alias].[class property mapping] can also be applied, and
     * will result in the meta data id of the given class property mapping.
     *
     * @param path the path to the resource property mapping
     * @return the resource property mapping for the given path
     */
    ResourcePropertyMapping getResourcePropertyMappingByPath(String path);

    /**
     * Returns an array of all the given {@link org.compass.core.mapping.ResourcePropertyMapping} for the given
     * path. If the path is in "dot path" notation, will reutrn a single mappings matching it (see
     * {@link #getResourcePropertyMappingByPath(String)}). Otherwise will return all the ones mapped to
     * the given name.
     */
    ResourcePropertyMapping[] getResourcePropertyMappingsByPath(String path);
}
