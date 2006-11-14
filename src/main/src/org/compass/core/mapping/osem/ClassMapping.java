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

package org.compass.core.mapping.osem;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.AbstractResourceMapping;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.PostProcessingMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class ClassMapping extends AbstractResourceMapping implements ResourceMapping, PostProcessingMapping {

    private PropertyPath classPath;

    private Class clazz;

    private boolean poly;

    private Class polyClass;

    private Boolean supportUnmarshall;

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private ClassPropertyMapping[] classPropertyMappings;

    private ClassIdPropertyMapping[] classIdPropertyMappings;

    private HashMap pathMappings;

    private Constructor constructor;

    private Constructor polyConstructor;

    public Mapping copy() {
        ClassMapping copy = new ClassMapping();
        super.copy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setClazz(getClazz());
        copy.setPolyClass(getPolyClass());
        copy.setConstructor(getConstructor());
        copy.setPolyConstructor(getPolyConstructor());
        copy.supportUnmarshall = supportUnmarshall;
        return copy;
    }

    public AliasMapping shallowCopy() {
        ClassMapping copy = new ClassMapping();
        super.shallowCopy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setClazz(getClazz());
        copy.setPolyClass(getPolyClass());
        copy.setConstructor(getConstructor());
        copy.setPolyConstructor(getPolyConstructor());
        copy.supportUnmarshall = supportUnmarshall;
        return copy;
    }

    /**
     * Post process by using the dynamic find operations to cache them.
     *
     * @throws MappingException
     */
    protected void doPostProcess() throws MappingException {
        PostProcessMappingCallback callback = new PostProcessMappingCallback();
        // since we do not perform static bindings when no unmarshalling, we will get OOME
        // (we do not copy the mappings or use max-depth)
        boolean recursive = isSupportUnmarshall();
        OsemMappingIterator.iterateMappings(callback, this, recursive);
        List findList = callback.getResourcePropertyMappings();
        resourcePropertyMappings = (ResourcePropertyMapping[])
                findList.toArray(new ResourcePropertyMapping[findList.size()]);
        findList = callback.getClassPropertyMappings();
        classPropertyMappings = (ClassPropertyMapping[]) findList.toArray(new ClassPropertyMapping[findList.size()]);
        findList = findClassPropertyIdMappings();
        classIdPropertyMappings = (ClassIdPropertyMapping[]) findList.toArray(new ClassIdPropertyMapping[findList
                .size()]);
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
     * Dynamically finds all the {@link ClassIdPropertyMapping}s for the class.
     *
     * @return A list of the class {@link ClassIdPropertyMapping}s.
     */
    public List findClassPropertyIdMappings() {
        ArrayList idMappingList = new ArrayList();
        for (Iterator it = mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassIdPropertyMapping) {
                idMappingList.add(m);
            }
        }
        return idMappingList;
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return (ResourcePropertyMapping) pathMappings.get(path);
    }

    public boolean isIncludePropertiesWithNoMappingsInAll() {
        return true;
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
        return supportUnmarshall.booleanValue();
    }

    public void setSupportUnmarshall(boolean supportUnmarshall) {
        this.supportUnmarshall = Boolean.valueOf(supportUnmarshall);
    }

    public boolean isSupportUnmarshallSet() {
        return supportUnmarshall != null;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public Constructor getPolyConstructor() {
        return polyConstructor;
    }

    public void setPolyConstructor(Constructor polyConstructor) {
        this.polyConstructor = polyConstructor;
    }

    public static class PostProcessMappingCallback extends OsemMappingIterator.ClassPropertyAndResourcePropertyGatherer {

        private HashMap pathMappings = new HashMap();

        private ArrayList pathSteps = new ArrayList();

        private StringBuffer sb = new StringBuffer();


        /**
         * Since we did not process duplicate mappings, we need to replace them with the original mappings that
         * were processed (for example, we added intenral ids to it where needed).
         */
        protected void onDuplicateMapping(ClassMapping classMapping, ObjectMapping actualMapping, ObjectMapping duplicateMapping) {
            classMapping.replaceMapping(duplicateMapping, actualMapping);
        }

        private void addToPath(Mapping mapping) {
            pathSteps.add(mapping.getName());
        }

        private void removeFromPath(Mapping mapping) {
            if (pathSteps.size() > 0) {
                pathSteps.remove(pathSteps.size() - 1);
            }
        }

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            boolean retVal = super.onBeginMultipleMapping(classMapping, mapping);
            addToPath(mapping);
            return retVal;
        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
            super.onEndMultiplMapping(classMapping, mapping);
            removeFromPath(mapping);
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping mapping) {
            super.onClassPropertyMapping(classMapping, mapping);
            ResourcePropertyMapping resourcePropertyMapping = mapping.getIdMapping();
            pathMappings.put(currentPath(), resourcePropertyMapping);
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping mapping) {
            super.onResourcePropertyMapping(mapping);
            if (!mapping.isInternal()) {
                addToPath(mapping);
            }
            pathMappings.put(currentPath(), mapping);
            if (!mapping.isInternal()) {
                removeFromPath(mapping);
            }
        }

        public HashMap getPathMappings() {
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
