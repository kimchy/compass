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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.ResourcePropertyConverter;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.util.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class CompassMapping {

    /**
     * A simple lookup class, for a given path, will provide simple access to
     * it's path and value converter. Also supports path escaping ('a.b' will
     * result in a.b and not alias a and resource property b).
     */
    public final class ResourcePropertyLookup {

        private ResourcePropertyMapping resourcePropertyMapping;

        private String path;

        public ResourcePropertyLookup(String name) {
            // the path is escaped, so don't try to look it up
            if (name.charAt(0) == '\'' && name.charAt(name.length() - 1) == '\'') {
                path = name.substring(1, name.length() - 1);
            } else {
                this.resourcePropertyMapping = getResourcePropertyMappingByPath(name);
                if (resourcePropertyMapping == null) {
                    path = name;
                } else {
                    path = resourcePropertyMapping.getPath().getPath();
                }
            }
        }

        public String getPath() {
            return path;
        }

        public String getValue(Object value) {
            ResourcePropertyConverter converter;
            if (resourcePropertyMapping != null && resourcePropertyMapping.getConverter() != null) {
                converter = (ResourcePropertyConverter) resourcePropertyMapping.getConverter();
            } else {
                converter = (ResourcePropertyConverter) getConverterLookup().lookupConverter(
                        value.getClass());
            }
            return converter.toString(value, resourcePropertyMapping);
        }
    }

    private final HashMap mappings = new HashMap();

    private final HashMap rootMappingsByAlias = new HashMap();

    private final HashMap rootMappingsByClass = new HashMap();

    private final HashMap mappingsByClass = new HashMap();

    private ResourceMapping[] rootMappingsArr = new ResourceMapping[0];

    private ConverterLookup converterLookup;

    private final ConcurrentHashMap cachedRootMappingsByClass = new ConcurrentHashMap();

    private final ConcurrentHashMap cachedMappingsByClass = new ConcurrentHashMap();

    private final NullResourceMapping nullResourceMappingEntryInCache = new NullResourceMapping();

    private HashSet hasMutipleClassMappingByClass = new HashSet();

    private PropertyPath path;

    public CompassMapping() {
    }

    public CompassMapping copy(ConverterLookup converterLookup) {
        CompassMapping copy = new CompassMapping();
        copy.converterLookup = converterLookup;
        copy.setPath(getPath());
        for (Iterator it = mappings.values().iterator(); it.hasNext();) {
            AliasMapping copyMapping = (AliasMapping) ((AliasMapping) it.next()).copy();
            copy.addMapping(copyMapping);
        }
        return copy;
    }

    public void clearMappings() {
        mappings.clear();
        rootMappingsByAlias.clear();
        rootMappingsByClass.clear();
        rootMappingsArr = new ResourceMapping[0];
        cachedRootMappingsByClass.clear();
        hasMutipleClassMappingByClass.clear();
        mappingsByClass.clear();
    }

    public ResourcePropertyLookup getResourcePropertyLookup(String name) {
        return new ResourcePropertyLookup(name);
    }

    public void addMapping(AliasMapping mapping) throws MappingException {
        if (mappings.get(mapping.getAlias()) != null) {
            throw new MappingException("Compass does not allow multiple aliases for alias [" + mapping.getAlias() + "]");
        }
        mappings.put(mapping.getAlias(), mapping);
        if (mapping instanceof ResourceMapping) {
            ResourceMapping resourceMapping = (ResourceMapping) mapping;
            if (resourceMapping.isRoot()) {
                rootMappingsByAlias.put(mapping.getAlias(), mapping);
                if (resourceMapping instanceof ClassMapping) {
                    ClassMapping cMapping = (ClassMapping) mapping;
                    if (rootMappingsByClass.get(cMapping.getName()) != null) {
                        hasMutipleClassMappingByClass.add(cMapping.getName());
                    }
                    rootMappingsByClass.put(cMapping.getName(), mapping);
                    mappingsByClass.put(cMapping.getName(), cMapping);
                }
                ResourceMapping[] result = new ResourceMapping[rootMappingsArr.length + 1];
                int i;
                for (i = 0; i < rootMappingsArr.length; i++) {
                    result[i] = rootMappingsArr[i];
                }
                result[i] = resourceMapping;
                rootMappingsArr = result;
            } else {
                if (resourceMapping instanceof ClassMapping) {
                    ClassMapping cMapping = (ClassMapping) mapping;
                    mappingsByClass.put(cMapping.getName(), cMapping);
                }
            }
        }
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

    public Iterator mappingsIt() {
        return mappings.values().iterator();
    }

    public AliasMapping getAliasMapping(String alias) {
        return (AliasMapping) mappings.get(alias);
    }

    public ResourceMapping getResourceMappingByAlias(String alias) {
        return (ResourceMapping) mappings.get(alias);
    }

    public ResourceMapping getRootMappingByAlias(String alias) {
        return (ResourceMapping) rootMappingsByAlias.get(alias);
    }

    public boolean hasRootMappingByAlias(String alias) {
        return rootMappingsByAlias.get(alias) != null;
    }

    public boolean hasClassMapping(String alias) {
        return (rootMappingsByAlias.get(alias) instanceof ClassMapping);
    }

    public boolean hasRawResourceMapping(String alias) {
        return (rootMappingsByAlias.get(alias) instanceof RawResourceMapping);
    }

    public boolean hasMultipleClassMapping(String className) {
        return hasMutipleClassMappingByClass.contains(className);
    }

    public ClassMapping getDirectClassMappingByClass(Class clazz) {
        return (ClassMapping) mappingsByClass.get(clazz.getName());
    }

    /**
     * Finds the Class mapping that is the "nearest" to the provided class.
     * Similar way that {@link #findRootMappingByClass(Class)} except the search
     * is on all the ClassMappings (even ones that are not marked as root).
     */
    public ClassMapping getClassMappingByClass(Class clazz) {
        return (ClassMapping) doGetResourceMappingByClass(clazz, false, mappingsByClass, cachedMappingsByClass);
    }

    /**
     * Finds a root mapping by the class name. If a root mapping is not found
     * for the class name, than searches for mappings for the interfaces, if not
     * found, checks for subclasses, and subclassess interfaces. Note: If there
     * is no direct mapping that match the class name, than the mapping that is
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

    private ResourceMapping doGetResourceMappingByClass(Class clazz, boolean throwEx,
                                                        HashMap mappingClassMap, ConcurrentHashMap cachedMappingsMap) throws MappingException {
        // we don't really care that we might execute it twice (for caching)
        ResourceMapping rm = (ResourceMapping) cachedMappingsMap.get(clazz);
        if (rm != null) {
            if (rm == nullResourceMappingEntryInCache) {
                if (throwEx) {
                    throw new MappingException("Failed to find any mappings for class [" + clazz.getName() + "]");
                }
                return null;
            }
            return rm;
        }
        rm = doGetActualResourceMappingByClass(clazz, mappingClassMap);
        if (rm == null) {
            cachedMappingsMap.put(clazz, nullResourceMappingEntryInCache);
            if (throwEx) {
                throw new MappingException("Failed to find any mappings for class [" + clazz.getName() + "]");
            }
            return null;
        } else {
            cachedMappingsMap.put(clazz, rm);
        }
        return rm;
    }

    private ResourceMapping doGetActualResourceMappingByClass(Class clazz, HashMap mappingClassMap) {
        ResourceMapping rm = (ResourceMapping) mappingClassMap.get(clazz.getName());
        if (rm != null) {
            return rm;
        }
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            rm = (ResourceMapping) mappingClassMap.get(interfaces[i].getName());
            if (rm != null) {
                return rm;
            }
        }
        Class superClass = clazz.getSuperclass();
        if (superClass == null) {
            return null;
        }
        return doGetActualResourceMappingByClass(superClass, mappingClassMap);
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
}
