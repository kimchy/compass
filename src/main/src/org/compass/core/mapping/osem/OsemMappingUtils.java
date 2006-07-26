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

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author kimchy
 */
public abstract class OsemMappingUtils {

    public static interface ClassMappingCallback {

        void onBeginMultipleMapping(Mapping mapping);

        void onEndMultiplMapping(Mapping mapping);

        void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onEndCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onClassPropertyMapping(ClassPropertyMapping classPropertyMapping);

        void onComponentMapping(ComponentMapping componentMapping);

        void onReferenceMapping(ReferenceMapping referenceMapping);

        void onConstantMetaDataMappaing(ConstantMetaDataMapping constantMetaDataMapping);

        void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping);
    }

    public static class ClassPropertyAndResourcePropertyGatherer implements ClassMappingCallback {

        private ArrayList classPropertyMappings = new ArrayList();

        private ArrayList resourcePropertyMappings = new ArrayList();

        public void addResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
            resourcePropertyMappings.add(resourcePropertyMapping);
        }

        public ArrayList getClassPropertyMappings() {
            return classPropertyMappings;
        }

        public ArrayList getResourcePropertyMappings() {
            return resourcePropertyMappings;
        }

        public void onBeginMultipleMapping(Mapping mapping) {
        }

        public void onEndMultiplMapping(Mapping mapping) {
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassPropertyMapping classPropertyMapping) {
            classPropertyMappings.add(classPropertyMapping);
        }

        public void onComponentMapping(ComponentMapping componentMapping) {
        }

        public void onReferenceMapping(ReferenceMapping referenceMapping) {
        }

        public void onConstantMetaDataMappaing(ConstantMetaDataMapping constantMetaDataMapping) {
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
            resourcePropertyMappings.add(resourcePropertyMapping);
        }

    }

    public static class ClassPropertyAndResourcePropertyGathererAndPathBuilder extends
            ClassPropertyAndResourcePropertyGatherer {

        private HashMap pathMappings = new HashMap();

        private ArrayList pathSteps = new ArrayList();
        
        private StringBuffer sb = new StringBuffer();
        
        private void addToPath(Mapping mapping) {
            pathSteps.add(mapping.getName());
        }

        private void removeFromPath(Mapping mapping) {
            if (pathSteps.size() > 0) {
                pathSteps.remove(pathSteps.size() - 1);
            }
        }

        public void onBeginMultipleMapping(Mapping mapping) {
            super.onBeginMultipleMapping(mapping);
            addToPath(mapping);
        }

        public void onEndMultiplMapping(Mapping mapping) {
            super.onEndMultiplMapping(mapping);
            removeFromPath(mapping);
        }

        public void onClassPropertyMapping(ClassPropertyMapping mapping) {
            super.onClassPropertyMapping(mapping);
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


    public static void iterateMappings(ClassMappingCallback callback, Iterator mappingsIt) {
        for (; mappingsIt.hasNext();) {
            Mapping m = (Mapping) mappingsIt.next();
            if (m instanceof ClassPropertyMapping) {
                ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) m;
                callback.onBeginMultipleMapping(classPropertyMapping);
                callback.onClassPropertyMapping(classPropertyMapping);
                for (Iterator resIt = classPropertyMapping.mappingsIt(); resIt.hasNext();) {
                    ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) resIt.next();
                    callback.onResourcePropertyMapping(resourcePropertyMapping);
                }
                callback.onEndMultiplMapping(classPropertyMapping);
            } else if (m instanceof ComponentMapping) {
                ComponentMapping componentMapping = (ComponentMapping) m;
                callback.onBeginMultipleMapping(componentMapping);

                callback.onComponentMapping(componentMapping);
                ClassMapping[] refMappings = componentMapping.getRefClassMappings();
                for (int i = 0; i < refMappings.length; i++) {
                    OsemMappingUtils.iterateMappings(callback, refMappings[i].mappingsIt());
                }

                callback.onEndMultiplMapping(componentMapping);
            } else if (m instanceof ReferenceMapping) {
                ReferenceMapping referenceMapping = (ReferenceMapping) m;
                callback.onBeginMultipleMapping(referenceMapping);

                callback.onReferenceMapping(referenceMapping);
                ClassMapping[] refMappings = referenceMapping.getRefClassMappings();
                for (int i = 0; i < refMappings.length; i++) {
                    OsemMappingUtils.iterateMappings(callback, refMappings[i].mappingsIt());
                }

                if (referenceMapping.getRefCompMapping() != null) {
                    OsemMappingUtils.iterateMappings(callback, referenceMapping.getRefCompMapping().mappingsIt());
                }

                callback.onEndMultiplMapping(referenceMapping);
            } else if (m instanceof ConstantMetaDataMapping) {
                ConstantMetaDataMapping constantMetaDataMapping = (ConstantMetaDataMapping) m;
                callback.onBeginMultipleMapping(constantMetaDataMapping);

                callback.onResourcePropertyMapping(constantMetaDataMapping);

                callback.onEndMultiplMapping(constantMetaDataMapping);
            } else if (m instanceof AbstractCollectionMapping) {
                // collection, add the internal element attributes
                AbstractCollectionMapping colMapping = (AbstractCollectionMapping) m;
                callback.onBeginCollectionMapping(colMapping);
                Mapping elementMapping = colMapping.getElementMapping();
                if (elementMapping instanceof ClassPropertyMapping) {
                    ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) elementMapping;
                    callback.onBeginMultipleMapping(classPropertyMapping);
                    callback.onClassPropertyMapping(classPropertyMapping);
                    for (Iterator resIt = classPropertyMapping.mappingsIt(); resIt.hasNext();) {
                        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) resIt.next();
                        callback.onResourcePropertyMapping(resourcePropertyMapping);
                    }
                    callback.onEndMultiplMapping(classPropertyMapping);
                } else if (elementMapping instanceof ComponentMapping) {
                    ComponentMapping componentMapping = (ComponentMapping) elementMapping;
                    callback.onBeginMultipleMapping(componentMapping);

                    callback.onComponentMapping(componentMapping);
                    ClassMapping[] refMappings = componentMapping.getRefClassMappings();
                    for (int i = 0; i < refMappings.length; i++) {
                        OsemMappingUtils.iterateMappings(callback, refMappings[i].mappingsIt());
                    }

                    callback.onEndMultiplMapping(componentMapping);
                } else if (elementMapping instanceof ReferenceMapping) {
                    ReferenceMapping referenceMapping = (ReferenceMapping) elementMapping;
                    callback.onBeginMultipleMapping(referenceMapping);

                    callback.onReferenceMapping(referenceMapping);
                    callback.onReferenceMapping(referenceMapping);
                    ClassMapping[] refMappings = referenceMapping.getRefClassMappings();
                    for (int i = 0; i < refMappings.length; i++) {
                        OsemMappingUtils.iterateMappings(callback, refMappings[i].mappingsIt());
                    }

                    if (referenceMapping.getRefCompMapping() != null) {
                        OsemMappingUtils.iterateMappings(callback, referenceMapping.getRefCompMapping().mappingsIt());
                    }

                    callback.onEndMultiplMapping(referenceMapping);
                }
                callback.onEndCollectionMapping(colMapping);
            }
        }
    }

}
