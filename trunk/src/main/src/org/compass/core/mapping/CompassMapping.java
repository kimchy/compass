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

package org.compass.core.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;

/**
 * @author kimchy
 */
public class CompassMapping {

    private final Map<String, AliasMapping> mappings = new HashMap<String, AliasMapping>();

    private final Map<String, ResourceMapping> rootMappingsByAlias = new HashMap<String, ResourceMapping>();

    private final Map<String, ResourceMapping> nonRootMappingsByAlias = new HashMap<String, ResourceMapping>();

    private final ResourceMappingsByNameHolder mappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder rootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedRootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder nonRootMappingsByClass = new ResourceMappingsByNameHolder();

    private final ResourceMappingsByNameHolder cachedNonRootMappingsByClass = new ResourceMappingsByNameHolder();

    private ResourceMapping[] rootMappingsArr = new ResourceMapping[0];

    private ConverterLookup converterLookup;

    private final NullResourceMapping nullResourceMappingEntryInCache = new NullResourceMapping();

    private final HashMap<String, ResourcePropertyMapping[]> resourcePropertyMappingByPath = new HashMap<String, ResourcePropertyMapping[]>();

    private PropertyPath path;

    public CompassMapping() {
    }

    public CompassMapping copy(ConverterLookup converterLookup) {
        CompassMapping copy = new CompassMapping();
        copy.converterLookup = converterLookup;
        copy.setPath(getPath());
        for (AliasMapping aliasMapping : mappings.values()) {
            AliasMapping copyMapping = (AliasMapping) (aliasMapping).copy();
            copy.addMapping(copyMapping);
        }
        return copy;
    }

    public void postProcess() {
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
    }

    public void clearMappings() {
        mappings.clear();

        rootMappingsByAlias.clear();
        nonRootMappingsByAlias.clear();

        mappingsByClass.clear();
        cachedMappingsByClass.clear();

        rootMappingsByClass.clear();
        cachedRootMappingsByClass.clear();

        nonRootMappingsByClass.clear();
        cachedNonRootMappingsByClass.clear();

        rootMappingsArr = new ResourceMapping[0];

        resourcePropertyMappingByPath.clear();
    }

    public void addMapping(AliasMapping mapping) throws MappingException {
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
        ResourceMapping resourceMapping = getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            throw new IllegalArgumentException("Failed to find class/resource mapping for alias [" + alias
                    + "] from path [" + path + "]");
        }
        ResourcePropertyMapping resourcePropertyMapping = resourceMapping.getResourcePropertyMappingByDotPath(path.substring(dotIndex + 1));
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
     * Returns an itertor over all the current mappings.
     */
    public Iterator mappingsIt() {
        return mappings.values().iterator();
    }

    /**
     * Returns the alias mapping for the given alias (most if not all of the times, this will be a {@link org.compass.core.mapping.ResourceMapping}).
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

        void addMapping(String name, ResourceMapping resourceMapping) {
            List<ResourceMapping> l = mappings.get(name);
            if (l == null) {
                l = new ArrayList<ResourceMapping>();
                mappings.put(name, l);
            }
            l.add(resourceMapping);
        }

        public void clear() {
            mappings.clear();
        }

        public List<ResourceMapping> getMappingsByName(String name) {
            return mappings.get(name);
        }

        public List<ResourceMapping> getUnmodifiableMappingsByName(String name) {
            return Collections.unmodifiableList(mappings.get(name));
        }

        /**
         * Returns the first class mapping matching the given name. Returns
         * <code>null</code> of no mapping matches the name.
         */
        public ResourceMapping getResourceMappingByName(String name) {
            List<ResourceMapping> l = getMappingsByName(name);
            if (l == null) {
                return null;
            }
            return l.get(l.size() - 1);
        }

        public boolean hasMultipleMappingsByName(String name) {
            List<ResourceMapping> l = getMappingsByName(name);
            return l != null && l.size() > 1;
        }
    }
}
