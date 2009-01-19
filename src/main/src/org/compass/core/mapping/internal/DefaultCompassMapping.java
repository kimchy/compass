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

package org.compass.core.mapping.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.support.NullResourceMapping;

/**
 * @author kimchy
 */
public class DefaultCompassMapping implements InternalCompassMapping {

    private final Map<String, AliasMapping> mappings = new ConcurrentHashMap<String, AliasMapping>();

    private final Map<String, ResourceMapping> rootMappingsByAlias = new ConcurrentHashMap<String, ResourceMapping>();

    private final Map<String, ResourceMapping> nonRootMappingsByAlias = new ConcurrentHashMap<String, ResourceMapping>();

    private final ResourceMappingsByNameHolder mappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder rootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedRootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder nonRootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedNonRootMappingsByClass = new ResourceMappingsByNameHolder();

    private ResourceMapping[] rootMappingsArr = new ResourceMapping[0];

    private ConverterLookup converterLookup;

    private final NullResourceMapping nullResourceMappingEntryInCache = new NullResourceMapping();

    private final Map<String, ResourcePropertyMapping[]> resourcePropertyMappingByPath = new ConcurrentHashMap<String, ResourcePropertyMapping[]>();

    private PropertyPath path;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public DefaultCompassMapping() {
    }

    public InternalCompassMapping copy(ConverterLookup converterLookup) {
        writeLock.lock();
        try {
            DefaultCompassMapping copy = new DefaultCompassMapping();
            copy.converterLookup = converterLookup;
            copy.setPath(getPath());
            for (AliasMapping aliasMapping : mappings.values()) {
                AliasMapping copyMapping = (AliasMapping) (aliasMapping).copy();
                copy.addMapping(copyMapping);
            }
            return copy;
        } finally {
            writeLock.unlock();
        }
    }

    public void postProcess() {
        writeLock.lock();
        try {
            ResourceMapping[] rootMappings = getRootMappings();
            for (ResourceMapping rootMapping : rootMappings) {
                // update the resource property mapping
                ResourcePropertyMapping[] resourcePropertyMappings = rootMapping.getResourcePropertyMappings();
                for (ResourcePropertyMapping rpm : resourcePropertyMappings) {
                    if (rpm.getPath() != null) {
                        String path = rpm.getPath().getPath();
                        ResourcePropertyMapping[] rpms = resourcePropertyMappingByPath.get(path);
                        if (rpms == null) {
                            resourcePropertyMappingByPath.put(path, new ResourcePropertyMapping[]{rpm});
                        } else {
                            ResourcePropertyMapping[] tmpRpms = new ResourcePropertyMapping[rpms.length + 1];
                            System.arraycopy(rpms, 0, tmpRpms, 0, rpms.length);
                            tmpRpms[tmpRpms.length - 1] = rpm;
                            resourcePropertyMappingByPath.put(path, tmpRpms);
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void clearMappings() {
        writeLock.lock();
        try {
            mappings.clear();

            rootMappingsByAlias.clear();
            nonRootMappingsByAlias.clear();

            mappingsByClass.clear();

            rootMappingsByClass.clear();
            nonRootMappingsByClass.clear();

            rootMappingsArr = new ResourceMapping[0];

            resourcePropertyMappingByPath.clear();

            clearCache();
        } finally {
            writeLock.unlock();
        }
    }

    public void clearCache() {
        writeLock.lock();
        try {
            cachedMappingsByClass.clear();
            cachedRootMappingsByClass.clear();
            cachedNonRootMappingsByClass.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds the given Alias mapping.
     */
    public void addMapping(AliasMapping mapping) throws MappingException {
        writeLock.lock();
        try {
            if (mappings.get(mapping.getAlias()) != null) {
                throw new MappingException("Compass does not allow multiple aliases for alias [" + mapping.getAlias() + "]");
            }
            mappings.put(mapping.getAlias(), mapping);
            if (mapping instanceof ResourceMapping) {
                ResourceMapping resourceMapping = (ResourceMapping) mapping;
                if (resourceMapping.isRoot()) {
                    rootMappingsByAlias.put(mapping.getAlias(), resourceMapping);
                    if (resourceMapping instanceof ClassMapping) {
                        rootMappingsByClass.addMapping(resourceMapping.getName(), resourceMapping);
                        mappingsByClass.addMapping(resourceMapping.getName(), resourceMapping);

                    }
                    ResourceMapping[] result = new ResourceMapping[rootMappingsArr.length + 1];
                    int i;
                    for (i = 0; i < rootMappingsArr.length; i++) {
                        result[i] = rootMappingsArr[i];
                    }
                    result[i] = resourceMapping;
                    rootMappingsArr = result;
                } else {
                    nonRootMappingsByAlias.put(mapping.getAlias(), resourceMapping);
                    if (resourceMapping instanceof ClassMapping) {
                        mappingsByClass.addMapping(resourceMapping.getName(), resourceMapping);
                        nonRootMappingsByClass.addMapping(resourceMapping.getName(), resourceMapping);
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public boolean removeMappingByClass(String className) throws MappingException {
        writeLock.lock();
        try {
            boolean result = false;
            List<ResourceMapping> resourceMappings = mappingsByClass.getMappingsByName(className);
            if (resourceMappings != null) {
                ResourceMapping[] rm = resourceMappings.toArray(new ResourceMapping[resourceMappings.size()]);
                for (ResourceMapping resourceMapping : rm) {
                    result |= removeMappingByAlias(resourceMapping.getAlias());
                }
            }
            return result;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean removeMappingByAlias(String alias) throws MappingException {
        writeLock.lock();
        try {
            AliasMapping aliasMapping = mappings.remove(alias);
            if (aliasMapping == null) {
                return false;
            }
            if (aliasMapping instanceof ResourceMapping) {
                ResourceMapping resourceMapping = (ResourceMapping) aliasMapping;
                if (resourceMapping.isRoot()) {
                    rootMappingsByAlias.remove(alias);
                    if (resourceMapping instanceof ClassMapping) {
                        rootMappingsByClass.removeMapping(resourceMapping);
                        mappingsByClass.removeMapping(resourceMapping);
                    }
                    List<ResourceMapping> l = new ArrayList<ResourceMapping>(rootMappingsArr.length);
                    for (ResourceMapping rm : rootMappingsArr) {
                        if (rm != resourceMapping) {
                            l.add(resourceMapping);
                        }
                    }
                    rootMappingsArr = l.toArray(new ResourceMapping[l.size()]);
                } else {
                    nonRootMappingsByAlias.remove(alias);
                    if (resourceMapping instanceof ClassMapping) {
                        mappingsByClass.removeMapping(resourceMapping);
                        nonRootMappingsByClass.removeMapping(resourceMapping);
                    }
                }
            }
            clearCache();
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns a resoruce lookup for a specific name. Supports dot path notation ([alias].[class property].).
     * Allows to get the meta-data/resource property mapping through it (or a list of mappings).
     */
    public ResourcePropertyLookup getResourcePropertyLookup(String name) throws IllegalArgumentException {
        return new ResourcePropertyLookup(this, name);
    }

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
    public ResourcePropertyMapping getResourcePropertyMappingByPath(String path) {
        int dotIndex = path.indexOf('.');
        if (dotIndex == -1) {
            return null;
        }
        String alias = path.substring(0, dotIndex);
        String propertyName = path.substring(dotIndex + 1);
        AliasMapping aliasMapping = getAliasMapping(alias);
        ResourcePropertyMapping resourcePropertyMapping = null;
        if (aliasMapping instanceof ResourceMapping) {
            resourcePropertyMapping = ((ResourceMapping) aliasMapping).getResourcePropertyMappingByDotPath(propertyName);
        } else if (aliasMapping != null) {
            // go over alias mappings (such as contract mappings) and try and find if someone that extends it
            // defines mappings for this dot path notation (since we only post process root resource mappings).
            // note, if the extedning resource mapping also overrides the meta-data, then it will use it and
            // not the one defined within the contract mapping.
            String[] extendingAliases = aliasMapping.getExtendingAliases();
            if (extendingAliases != null) {
                for (String extendingAlias : extendingAliases) {
                    ResourceMapping resourceMapping = getRootMappingByAlias(extendingAlias);
                    if (resourceMapping != null) {
                        resourcePropertyMapping = resourceMapping.getResourcePropertyMappingByDotPath(propertyName);
                        if (resourcePropertyMapping != null) {
                            break;
                        }
                    }
                }
            }
        }
        if (resourcePropertyMapping == null) {
            throw new IllegalArgumentException("Failed to find mapping for alias [" + alias + "] and path [" + path + "]");
        }
        return resourcePropertyMapping;
    }

    /**
     * Returns an array of all the given {@link org.compass.core.mapping.ResourcePropertyMapping} for the given
     * path. If the path is in "dot path" notation, will reutrn a single mappings matching it (see
     * {@link #getResourcePropertyMappingByPath(String)}). Otherwise will return all the ones mapped to
     * the given name.
     */
    public ResourcePropertyMapping[] getResourcePropertyMappingsByPath(String path) {
        int dotIndex = path.indexOf('.');
        if (dotIndex != -1) {
            return new ResourcePropertyMapping[]{getResourcePropertyMappingByPath(path)};
        }
        return resourcePropertyMappingByPath.get(path);
    }

    /**
     * Returns an array of all the current mappings.
     */
    public AliasMapping[] getMappings() {
        return mappings.values().toArray(new AliasMapping[mappings.size()]);
    }

    /**
     * Returns the alias mapping for the given alias (most if not all of the times, this will
     * be a {@link org.compass.core.mapping.ResourceMapping}).
     */
    public AliasMapping getAliasMapping(String alias) {
        return mappings.get(alias);
    }

    /**
     * Returns the resource mapping for the given alias.
     */
    public ResourceMapping getMappingByAlias(String alias) {
        return (ResourceMapping) mappings.get(alias);
    }

    /**
     * Returns the root resource mapping associated with the alias. Retruns
     * <code>null</code> if no root mapping (or no mapping) is associated with the alias.
     */
    public ResourceMapping getRootMappingByAlias(String alias) {
        return rootMappingsByAlias.get(alias);
    }

    /**
     * Returns the non root resource mapping associated with the alias. Retruns
     * <code>null</code> if no non root mapping (or no mapping) is associated with the alias.
     */
    public ResourceMapping getNonRootMappingByAlias(String alias) {
        return nonRootMappingsByAlias.get(alias);
    }

    /**
     * Returns <code>true</code> if the given alias has a root resource mapping.
     */
    public boolean hasRootMappingByAlias(String alias) {
        return rootMappingsByAlias.get(alias) != null;
    }

    /**
     * Returns <code>true</code> if there is a <b>root</b> {@link org.compass.core.mapping.osem.ClassMapping}
     * for the given alias.
     */
    public boolean hasRootClassMapping(String alias) {
        return (rootMappingsByAlias.get(alias) instanceof ClassMapping);
    }

    /**
     * Returns <code>true</code> if there is a <b>root</b> {@link org.compass.core.mapping.rsem.RawResourceMapping}
     * for the given alias.
     */
    public boolean hasRootRawResourceMapping(String alias) {
        return (rootMappingsByAlias.get(alias) instanceof RawResourceMapping);
    }

    /**
     * Returns <code>true</code> if the given <b>className</b> has multiple class mappings.
     */
    public boolean hasMultipleRootClassMapping(String className) {
        return rootMappingsByClass.hasMultipleMappingsByName(className);
    }

    /**
     * Returns the direct class mapping for the given class (root or not). Will not try to
     * navigate up the interface/superclass in order to find the "nearset" class mapping.
     *
     * <p>If a class has more than one mappings (using differnet aliases) will return the
     * first one.
     */
    public ResourceMapping getDirectMappingByClass(Class clazz) {
        return mappingsByClass.getResourceMappingByName(clazz.getName());
    }

    /**
     * Returns all the direct class mapping for the given class (root or not). Will not
     * try to navigate up the interface/superclass in order to find the "nearest" class
     * mapping.
     */
    public List<ResourceMapping> getAllDirectMappingByClass(Class clazz) {
        return mappingsByClass.getUnmodifiableMappingsByName(clazz.getName());
    }


    public boolean hasMappingForClass(Class clazz, Cascade cascade) {
        ResourceMapping resourceMapping = getRootMappingByClass(clazz);
        if (resourceMapping != null) {
            return true;
        }
        resourceMapping = getNonRootMappingByClass(clazz);
        return resourceMapping != null && resourceMapping.operationAllowed(cascade);
    }

    public boolean hasMappingForAlias(String alias, Cascade cascade) {
        ResourceMapping resourceMapping = getRootMappingByAlias(alias);
        if (resourceMapping != null) {
            return true;
        }
        resourceMapping = getNonRootMappingByAlias(alias);
        return resourceMapping != null && resourceMapping.operationAllowed(cascade);
    }

    /**
     * Finds the Resource mapping that is the "nearest" to the provided class.
     * Similar way that {@link #findRootMappingByClass(Class)} except the search
     * is on all the ClassMappings (even ones that are not marked as root).
     */
    public ResourceMapping getMappingByClass(Class clazz) {
        return doGetResourceMappingByClass(clazz, false, mappingsByClass, cachedMappingsByClass);
    }

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
    public ResourceMapping findRootMappingByClass(Class clazz) throws MappingException {
        return doGetResourceMappingByClass(clazz, true, rootMappingsByClass, cachedRootMappingsByClass);
    }

    /**
     * Does exactly the same as {@link #findRootMappingByClass(Class)}, but returns <code>null</code>
     * if nothing is found (does not throw an exception).
     */
    public ResourceMapping getRootMappingByClass(Class clazz) throws MappingException {
        return doGetResourceMappingByClass(clazz, false, rootMappingsByClass, cachedRootMappingsByClass);
    }

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
    public ResourceMapping findNonRootMappingByClass(Class clazz) throws MappingException {
        return doGetResourceMappingByClass(clazz, true, nonRootMappingsByClass, cachedNonRootMappingsByClass);
    }

    /**
     * Does exactly the same as {@link #findNonRootMappingByClass(Class)}, but returns <code>null</code>
     * if nothing is found (does not throw an exception).
     */
    public ResourceMapping getNonRootMappingByClass(Class clazz) throws MappingException {
        return doGetResourceMappingByClass(clazz, false, nonRootMappingsByClass, cachedNonRootMappingsByClass);
    }

    private ResourceMapping doGetResourceMappingByClass(Class clazz, boolean throwEx,
                                                        ResourceMappingsByNameHolder mappingByClass,
                                                        ResourceMappingsByNameHolder cachedMappingsByClass) throws MappingException {
        // we don't really care that we might execute it twice (for caching)
        String className = clazz.getName();
        ResourceMapping rm = cachedMappingsByClass.getResourceMappingByName(className);
        if (rm != null) {
            if (rm == nullResourceMappingEntryInCache) {
                if (throwEx) {
                    throw new MappingException("Failed to find any mappings for class [" + className + "]");
                }
                return null;
            }
            return rm;
        }
        rm = doGetActualResourceMappingByClass(clazz, mappingByClass);
        if (rm == null) {
            cachedMappingsByClass.addMapping(className, nullResourceMappingEntryInCache);
            if (throwEx) {
                throw new MappingException("Failed to find any mappings for class [" + className + "]");
            }
            return null;
        } else {
            cachedMappingsByClass.addMapping(className, rm);
        }
        return rm;
    }

    private ResourceMapping doGetActualResourceMappingByClass(Class clazz, ResourceMappingsByNameHolder mappingByClass) {
        ResourceMapping rm = mappingByClass.getResourceMappingByName(clazz.getName());
        if (rm != null) {
            return rm;
        }
        for (Class anInterface : clazz.getInterfaces()) {
            rm = mappingByClass.getResourceMappingByName(anInterface.getName());
            if (rm != null) {
                return rm;
            }
        }
        Class superClass = clazz.getSuperclass();
        if (superClass == null) {
            return null;
        }
        return doGetActualResourceMappingByClass(superClass, mappingByClass);
    }

    public ResourceMapping[] getRootMappings() {
        return rootMappingsArr;
    }

    public ConverterLookup getConverterLookup() {
        return converterLookup;
    }

    public PropertyPath getPath() {
        return path;
    }

    public void setPath(PropertyPath path) {
        this.path = path;
    }


    /**
     * A resource mapping holder based on a name (actually, any string). Holds a map keyed
     * by the name and the value a list of ResourceMapping registered under the name
     */
    private class ResourceMappingsByNameHolder {

        private final HashMap<String, List<ResourceMapping>> mappings = new HashMap<String, List<ResourceMapping>>();

        private ReadWriteLock rwl = new ReentrantReadWriteLock();

        void addMapping(String name, ResourceMapping resourceMapping) {
            rwl.writeLock().lock();
            try {
                List<ResourceMapping> l = mappings.get(name);
                if (l == null) {
                    l = new ArrayList<ResourceMapping>();
                    mappings.put(name, l);
                }
                l.add(resourceMapping);
            } finally {
                rwl.writeLock().unlock();
            }
        }

        void removeMapping(ResourceMapping resourceMapping) {
            rwl.writeLock().lock();
            try {
                for (List<ResourceMapping> l : mappings.values()) {
                    l.remove(resourceMapping);
                }
            } finally {
                rwl.writeLock().unlock();
            }
        }

        public void clear() {
            rwl.writeLock().lock();
            try {
                mappings.clear();
            } finally {
                rwl.writeLock().unlock();
            }
        }

        public List<ResourceMapping> getMappingsByName(String name) {
            rwl.readLock().lock();
            try {
                return mappings.get(name);
            } finally {
                rwl.readLock().unlock();
            }
        }

        public List<ResourceMapping> getUnmodifiableMappingsByName(String name) {
            rwl.readLock().lock();
            try {
                return Collections.unmodifiableList(mappings.get(name));
            } finally {
                rwl.readLock().unlock();
            }
        }

        /**
         * Returns the first class mapping matching the given name. Returns
         * <code>null</code> of no mapping matches the name.
         */
        public ResourceMapping getResourceMappingByName(String name) {
            rwl.readLock().lock();
            try {
                List<ResourceMapping> l = getMappingsByName(name);
                if (l == null) {
                    return null;
                }
                return l.get(l.size() - 1);
            } finally {
                rwl.readLock().unlock();
            }
        }

        public boolean hasMultipleMappingsByName(String name) {
            rwl.readLock().lock();
            try {
                List<ResourceMapping> l = getMappingsByName(name);
                return l != null && l.size() > 1;
            } finally {
                rwl.readLock().unlock();
            }
        }
    }
}
