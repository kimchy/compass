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

package org.compass.core.mapping.osem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.PostProcessingMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;
import org.compass.core.util.Assert;
import org.compass.core.util.reflection.ReflectionConstructor;

/**
 * @author kimchy
 */
public class ClassMapping extends AbstractResourceMapping implements ResourceMapping, PostProcessingMapping {

    private ManagedId managedId;

    private PropertyPath enumNamePath;

    private PropertyPath classPath;

    private PropertyPath basePath;

    private Class clazz;

    private boolean poly;

    private Class polyClass;

    private Boolean supportUnmarshall;

    private Boolean filterDuplicates;

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private ClassPropertyMapping[] classPropertyMappings;

    private ClassIdPropertyMapping[] classIdPropertyMappings;

    private HashMap<String, ResourcePropertyMapping> pathMappings;

    private ReflectionConstructor constructor;

    private ReflectionConstructor polyConstructor;

    public Mapping copy() {
        ClassMapping copy = new ClassMapping();
        super.copy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setEnumNamePath(getEnumNamePath());
        copy.setClazz(getClazz());
        copy.setPolyClass(getPolyClass());
        copy.setConstructor(getConstructor());
        copy.setPolyConstructor(getPolyConstructor());
        copy.supportUnmarshall = supportUnmarshall;
        copy.filterDuplicates = filterDuplicates;
        copy.setManagedId(getManagedId());
        copy.setBasePath(getBasePath());
        return copy;
    }

    public AliasMapping shallowCopy() {
        ClassMapping copy = new ClassMapping();
        super.shallowCopy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setEnumNamePath(getEnumNamePath());
        copy.setClazz(getClazz());
        copy.setPolyClass(getPolyClass());
        copy.setConstructor(getConstructor());
        copy.setPolyConstructor(getPolyConstructor());
        copy.supportUnmarshall = supportUnmarshall;
        copy.filterDuplicates = filterDuplicates;
        copy.setManagedId(getManagedId());
        copy.setBasePath(getBasePath());
        return copy;
    }

    /**
     * Post process by using the dynamic find operations to cache them.
     *
     * @throws MappingException
     */
    protected void doPostProcess() throws MappingException {
        PostProcessMappingCallback callback = new PostProcessMappingCallback();
        OsemMappingIterator.iterateMappings(callback, this, true);
        resourcePropertyMappings = callback.getResourcePropertyMappings().toArray(new ResourcePropertyMapping[callback.getResourcePropertyMappings().size()]);
        classPropertyMappings = callback.getClassPropertyMappings().toArray(new ClassPropertyMapping[callback.getClassPropertyMappings().size()]);
        List<ClassIdPropertyMapping> idMappings = findClassPropertyIdMappings();
        classIdPropertyMappings = idMappings.toArray(new ClassIdPropertyMapping[idMappings.size()]);
        pathMappings = callback.getPathMappings();
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return resourcePropertyMappings;
    }

    public ClassPropertyMapping[] getClassPropertyMappings() {
        return classPropertyMappings;
    }

    public ClassIdPropertyMapping[] getClassIdPropertyMappings() {
        return classIdPropertyMappings;
    }

    /**
     * Dynamically find the id mappings.
     */
    public List<Mapping> findIdMappings() {
        ArrayList<Mapping> idMappingList = new ArrayList<Mapping>();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassIdPropertyMapping) {
                idMappingList.add(m);
            }
            if (m instanceof IdComponentMapping) {
                idMappingList.add(m);
            }
        }
        return idMappingList;
    }

    /**
     * Dynamically finds all the {@link ClassIdPropertyMapping}s for the class.
     */
    public List<ClassIdPropertyMapping> findClassPropertyIdMappings() {
        ArrayList<ClassIdPropertyMapping> idMappingList = new ArrayList<ClassIdPropertyMapping>();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassIdPropertyMapping) {
                idMappingList.add((ClassIdPropertyMapping) m);
            }
            if (m instanceof IdComponentMapping) {
                IdComponentMapping idComponentMapping = (IdComponentMapping) m;
                idMappingList.addAll(idComponentMapping.getRefClassMappings()[0].findClassPropertyIdMappings());
            }
        }
        return idMappingList;
    }

    public List<ClassPropertyMapping> findClassPropertiesRequireProcessing() {
        ArrayList<ClassPropertyMapping> idMappingList = new ArrayList<ClassPropertyMapping>();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassIdPropertyMapping) {
                idMappingList.add((ClassIdPropertyMapping) m);
            } else if (m instanceof IdComponentMapping) {
                IdComponentMapping idComponentMapping = (IdComponentMapping) m;
                idMappingList.addAll(idComponentMapping.getRefClassMappings()[0].findClassPropertyIdMappings());
            } else if (m instanceof ClassPropertyMapping) {
                if (((ClassPropertyMapping) m).requiresIdProcessing()) {
                    idMappingList.add((ClassPropertyMapping) m);
                }
            }
        }
        return idMappingList;
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return pathMappings.get(path);
    }

    public ManagedId getManagedId() {
        return managedId;
    }

    public void setManagedId(ManagedId managedId) {
        this.managedId = managedId;
    }

    public boolean isPoly() {
        return poly;
    }

    public void setPoly(boolean poly) {
        this.poly = poly;
    }

    public PropertyPath getClassPath() {
        return classPath;
    }

    public void setClassPath(PropertyPath classPath) {
        this.classPath = classPath;
    }

    public PropertyPath getEnumNamePath() {
        return enumNamePath;
    }

    public void setEnumNamePath(PropertyPath enumNamePath) {
        this.enumNamePath = enumNamePath;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * In case poly is set to <code>true</code>, this will be the class that will
     * be instanciated for all persisted classes. If not set, Compass will persist
     * the actual class in the index, and will use it to instanciate the class.
     */
    public Class getPolyClass() {
        return polyClass;
    }

    public void setPolyClass(Class polyClass) {
        this.polyClass = polyClass;
    }

    public boolean isSupportUnmarshall() {
        // possible NPE, will take care of it in setting it
        return supportUnmarshall;
    }

    public void setSupportUnmarshall(boolean supportUnmarshall) {
        this.supportUnmarshall = supportUnmarshall;
    }

    public boolean isSupportUnmarshallSet() {
        return supportUnmarshall != null;
    }

    public Boolean isFilterDuplicates() {
        return filterDuplicates;
    }

    public void setFilterDuplicates(Boolean filterDuplicates) {
        this.filterDuplicates = filterDuplicates;
    }

    public ReflectionConstructor getConstructor() {
        return constructor;
    }

    public void setConstructor(ReflectionConstructor constructor) {
        this.constructor = constructor;
    }

    public ReflectionConstructor getPolyConstructor() {
        return polyConstructor;
    }

    public void setPolyConstructor(ReflectionConstructor polyConstructor) {
        this.polyConstructor = polyConstructor;
    }

    public PropertyPath getBasePath() {
        return basePath;
    }

    public void setBasePath(PropertyPath basePath) {
        this.basePath = basePath;
    }

    public class PostProcessMappingCallback extends OsemMappingIterator.ClassPropertyAndResourcePropertyGatherer {

        private HashMap<String, ResourcePropertyMapping> pathMappings = new HashMap<String, ResourcePropertyMapping>();

        private ArrayList<String> pathSteps = new ArrayList<String>();

        private StringBuilder sb = new StringBuilder();

        private Set<String> cyclicClassMappings = new HashSet<String>();

        class NoUnmarshallHolder {
            ClassMapping parent;
            ClassMapping classMapping;

            NoUnmarshallHolder(ClassMapping parent, ClassMapping classMapping) {
                this.parent = parent;
                this.classMapping = classMapping;
            }
        }

        /**
         * In case we do not need to support unmarshalling, we need to perform simple cyclic detection
         * and return <code>false</code> (won't iterate into this class mapping) if we already passed
         * this class mapping. We will remove the marker in the {@link #onEndClassMapping(ClassMapping)}.
         */
        public boolean onBeginClassMapping(ClassMapping classMapping) {
            if (classMapping.isSupportUnmarshall()) {
                return true;
            }
            if (cyclicClassMappings.contains(classMapping.getAlias())) {
                return false;
            }
            cyclicClassMappings.add(classMapping.getAlias());
            return true;
        }

        /**
         * If we do not support unmarshalling, we need to clean up our marker for this class mapping.
         */
        public void onEndClassMapping(ClassMapping classMapping) {
            if (classMapping.isSupportUnmarshall()) {
                return;
            }
            cyclicClassMappings.remove(classMapping.getAlias());
        }

        /**
         * <p>Since we did not process duplicate mappings, we need to replace them with the original mappings that
         * were processed (for example, we added intenral ids to it where needed).
         */
        protected void onDuplicateMapping(ClassMapping classMapping, ObjectMapping actualMapping, ObjectMapping duplicateMapping) {
            Assert.isTrue(actualMapping.getPropertyName().equals(duplicateMapping.getPropertyName()), "Internal Error in Compass, Original[" +
                    duplicateMapping.getName() + "] does not equal [" + actualMapping.getName() + "]");

            // TODO since we replace the mappings here, some attributes will be inacurate (like objClass) for the replaced class mapping
            int index = classMapping.mappings.indexOf(duplicateMapping);
            if (index < 0) {
                // let's look in the collection, if we find it as an element
                // then we just replace it (the duplicate mapping might raise
                // a duplicate for a collection, but with the collection element)
                for (int i = 0; i < classMapping.mappings.size(); i++) {
                    Object o = classMapping.mappings.get(i);
                    if (o instanceof AbstractCollectionMapping) {
                        AbstractCollectionMapping temp = (AbstractCollectionMapping) o;
                        if (temp.getElementMapping() == duplicateMapping) {
                            temp.setElementMapping(actualMapping);
                            index = i;
                            break;
                        }
                    }
                }
            } else {
                classMapping.mappingsByNameMap.put(duplicateMapping.getName(), actualMapping);
                classMapping.mappings.set(index, actualMapping);
            }
            if (index < 0) {
                throw new IllegalStateException("Internal Error in Compass, original mapping [" +
                        duplicateMapping.getName() + "] not found");
            }
        }

        private void addToPath(String name) {
            pathSteps.add(name);
        }

        private void removeFromPath() {
            if (pathSteps.size() > 0) {
                pathSteps.remove(pathSteps.size() - 1);
            }
        }

        /**
         */
        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            boolean retVal = super.onBeginMultipleMapping(classMapping, mapping);
            addToPath(mapping.getName());
            return retVal;
        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
            super.onEndMultiplMapping(classMapping, mapping);
            removeFromPath();
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping mapping) {
            super.onClassPropertyMapping(classMapping, mapping);
            ResourcePropertyMapping resourcePropertyMapping = mapping.getIdMapping();
            if (resourcePropertyMapping == null && mapping.mappingsSize() > 0) {
                resourcePropertyMapping = (ResourcePropertyMapping) mapping.mappingsIt().next();
            }
            pathMappings.put(currentPath(), resourcePropertyMapping);
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping mapping) {
            super.onResourcePropertyMapping(mapping);
            if (!mapping.isInternal()) {
                addToPath(mapping.getName());
            }
            pathMappings.put(currentPath(), mapping);
            if (!mapping.isInternal()) {
                removeFromPath();
            }

            if (mapping instanceof ClassPropertyMetaDataMapping) {
                if (!mapping.isInternal()) {
                    addToPath(((ClassPropertyMetaDataMapping) mapping).getOriginalName());
                }
                pathMappings.put(currentPath(), mapping);
                if (!mapping.isInternal()) {
                    removeFromPath();
                }
            }
        }

        public HashMap<String, ResourcePropertyMapping> getPathMappings() {
            return pathMappings;
        }

        private String currentPath() {
            sb.setLength(0);
            for (int i = 0; i < pathSteps.size(); i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append(pathSteps.get(i));
            }
            return sb.toString();
        }
    }

}
