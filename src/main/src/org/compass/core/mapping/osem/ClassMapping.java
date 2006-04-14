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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.compass.core.mapping.*;

/**
 * @author kimchy
 */
public class ClassMapping extends AbstractResourceMapping implements ResourceMapping, PostProcessingMapping {

    private String classPath;

    private Class clazz;

    private boolean poly;

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private ClassPropertyMapping[] classPropertyMappings;

    private ClassIdPropertyMapping[] classPropertyIdMappings;

    private HashMap pathMappings;

    public Mapping copy() {
        ClassMapping copy = new ClassMapping();
        super.copy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setClazz(getClazz());
        return copy;
    }

    public AliasMapping shallowCopy() {
        ClassMapping copy = new ClassMapping();
        super.shallowCopy(copy);
        copy.setPoly(isPoly());
        copy.setClassPath(getClassPath());
        copy.setClazz(getClazz());
        return copy;
    }

    /**
     * Post process by using the dynamic find operations to cache them.
     *
     * @throws MappingException
     */
    protected void doPostProcess() throws MappingException {
        OsemMappingUtils.ClassPropertyAndResourcePropertyGathererAndPathBuilder callback =
                new OsemMappingUtils.ClassPropertyAndResourcePropertyGathererAndPathBuilder();
        OsemMappingUtils.iterateMappings(callback, mappingsIt());
        List findList = callback.getResourcePropertyMappings();
        resourcePropertyMappings = (ResourcePropertyMapping[]) findList.toArray(new ResourcePropertyMapping[findList
                .size()]);
        findList = callback.getClassPropertyMappings();
        classPropertyMappings = (ClassPropertyMapping[]) findList.toArray(new ClassPropertyMapping[findList.size()]);
        findList = findClassPropertyIdMappings();
        classPropertyIdMappings = (ClassIdPropertyMapping[]) findList.toArray(new ClassIdPropertyMapping[findList
                .size()]);
        pathMappings = callback.getPathMappings();
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return resourcePropertyMappings;
    }

    public ClassPropertyMapping[] getClassPropertyMappings() {
        return classPropertyMappings;
    }

    public ClassIdPropertyMapping[] getClassPropertyIdMappings() {
        return classPropertyIdMappings;
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

    public ResourcePropertyMapping getMappingByPath(String path) {
        return (ResourcePropertyMapping) pathMappings.get(path);
    }

    public boolean isIncludePropertiesWithNoMappingsInAll() {
        return false;
    }

    public boolean isPoly() {
        return poly;
    }

    public void setPoly(boolean poly) {
        this.poly = poly;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
