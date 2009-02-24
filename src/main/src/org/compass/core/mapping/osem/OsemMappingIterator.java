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
import java.util.Iterator;
import java.util.List;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.Assert;

/**
 * @author kimchy
 */
public abstract class OsemMappingIterator {

    public static interface ClassMappingCallback {

        boolean onBeginClassMapping(ClassMapping classMapping);

        void onEndClassMapping(ClassMapping classMapping);

        boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping);

        void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping);

        void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onEndCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping);

        void onClassDynamicPropertyMapping(ClassMapping classMapping, ClassDynamicPropertyMapping dynamicPropertyMapping);

        void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping);

        void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping);

        void onCascadeMapping(ClassMapping classMapping, PlainCascadeMapping cascadeMapping);

        void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping);

        void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping);

        void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping);

        void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping);

        void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping);
    }

    /**
     * <p>Gathers both {@link org.compass.core.mapping.osem.ClassPropertyMapping}s
     * and {@link org.compass.core.mapping.ResourcePropertyMapping}s.
     *
     * <p>Also performs duplicate detection for referenced aliases. Duplicate mappings might occur
     * when the referenced alias is referencing several mappings (in case of the referenced class
     * actually contructing an object tree). Mappings that exist in the base class will be travesrsed
     * twice without the duplicate detection. The {@link #onBeginMultipleMapping(ClassMapping,org.compass.core.mapping.Mapping)}
     * detects such mappings, processes only the first one, and returns <code>false</code> for the rest
     * (denoting not to continue the investigation of this referenced mapping).
     */
    public static class ClassPropertyAndResourcePropertyGatherer implements ClassMappingCallback {

        private ArrayList<ClassPropertyMapping> classPropertyMappings = new ArrayList<ClassPropertyMapping>();

        private ArrayList<ResourcePropertyMapping> resourcePropertyMappings = new ArrayList<ResourcePropertyMapping>();

        private HashMap<Integer, HashMap<Object, HashMap<Object, ObjectMapping>>> ignoreInheritedDuplicatesClassMappings = new HashMap<Integer, HashMap<Object, HashMap<Object, ObjectMapping>>>();

        public ClassPropertyAndResourcePropertyGatherer() {

        }

        public List<ClassPropertyMapping> getClassPropertyMappings() {
            return classPropertyMappings;
        }

        public List<ResourcePropertyMapping> getResourcePropertyMappings() {
            return resourcePropertyMappings;
        }

        public boolean onBeginClassMapping(ClassMapping classMapping) {
            return true;
        }

        public void onEndClassMapping(ClassMapping classMapping) {
        }

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            // if it is ref alias mapping, mark its multiple ref class mappings as ones
            // to be considered for ignoring inherited duplicates
            if (mapping instanceof RefAliasObjectMapping) {
                ClassMapping[] classMappings = ((RefAliasObjectMapping) mapping).getRefClassMappings();
                if (classMappings.length > 1) {
                    HashMap<Object, HashMap<Object, ObjectMapping>> byAlias = new HashMap<Object, HashMap<Object, ObjectMapping>>();
                    for (ClassMapping classMapping1 : classMappings) {
                        ignoreInheritedDuplicatesClassMappings.put(System.identityHashCode(classMapping1), byAlias);
                    }
                }
            }
            // check if the class mapping is in the inherited duplicates checking
            // if so, only traverse the first one, and the rest of the duplicates
            // just ignore by returning false for drilling down
            HashMap<Object, HashMap<Object, ObjectMapping>> byAlias = ignoreInheritedDuplicatesClassMappings.get(new Integer(System.identityHashCode(classMapping)));
            if (byAlias != null && (mapping instanceof ObjectMapping)) {
                ObjectMapping objectMapping = (ObjectMapping) mapping;
                Assert.notNull(objectMapping.getDefinedInAlias(), "Internal Compass Error, Defined in Alias not found for [" +
                        objectMapping.getPropertyName() + "] in alias [" + classMapping.getAlias() + "]");
                HashMap<Object, ObjectMapping> propByAlias = byAlias.get(objectMapping.getDefinedInAlias());
                if (propByAlias == null) {
                    propByAlias = new HashMap<Object, ObjectMapping>();
                    byAlias.put(objectMapping.getDefinedInAlias(), propByAlias);
                }
                ObjectMapping actualObjectMapping = propByAlias.get(objectMapping.getPropertyName());
                if (actualObjectMapping != null) {
                    onDuplicateMapping(classMapping, actualObjectMapping, objectMapping);
                    return false;
                }
                propByAlias.put(objectMapping.getPropertyName(), objectMapping);
            }
            return true;
        }

        protected void onDuplicateMapping(ClassMapping classMapping, ObjectMapping actualMapping, ObjectMapping duplicateMapping) {

        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping) {
            classPropertyMappings.add(classPropertyMapping);
        }

        public void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping) {
        }

        public void onCascadeMapping(ClassMapping classMapping, PlainCascadeMapping cascadeMapping) {
        }

        public void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping) {
        }

        public void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping) {
        }

        public void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping) {
        }

        public void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping) {
        }

        public void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping) {
        }

        public void onClassDynamicPropertyMapping(ClassMapping classMapping, ClassDynamicPropertyMapping dynamicPropertyMapping) {
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
            resourcePropertyMappings.add(resourcePropertyMapping);
        }

    }

    public static void iterateMappings(ClassMappingCallback callback, ClassMapping classMapping) {
        iterateMappings(callback, classMapping, true);
    }

    public static void iterateMappings(ClassMappingCallback callback, ClassMapping classMapping, boolean recursive) {
        if (!callback.onBeginClassMapping(classMapping)) {
            return;
        }
        for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
            Mapping m = (Mapping) mappingsIt.next();
            if (m instanceof ClassPropertyMapping) {
                ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) m;
                iteratePropertyMapping(callback, classMapping, classPropertyMapping);
            } else if (m instanceof ClassDynamicPropertyMapping) {
                ClassDynamicPropertyMapping dynamicPropertyMapping = (ClassDynamicPropertyMapping) m;
                callback.onClassDynamicPropertyMapping(classMapping, dynamicPropertyMapping);
            } else if (m instanceof ParentMapping) {
                callback.onParentMapping(classMapping, (ParentMapping) m);
            } else if (m instanceof PlainCascadeMapping) {
                callback.onCascadeMapping(classMapping, (PlainCascadeMapping) m);
            } else if (m instanceof DynamicMetaDataMapping) {
                DynamicMetaDataMapping dynamicMetaDataMapping = (DynamicMetaDataMapping) m;
                callback.onDynamicMetaDataMapping(classMapping, dynamicMetaDataMapping);
                callback.onResourcePropertyMapping(dynamicMetaDataMapping);
            } else if (m instanceof ComponentMapping) {
                ComponentMapping componentMapping = (ComponentMapping) m;
                iterateComponentMapping(callback, classMapping, componentMapping, recursive);
            } else if (m instanceof ReferenceMapping) {
                ReferenceMapping referenceMapping = (ReferenceMapping) m;
                iterateReferenceMapping(callback, classMapping, referenceMapping, recursive);
            } else if (m instanceof ConstantMetaDataMapping) {
                ConstantMetaDataMapping constantMetaDataMapping = (ConstantMetaDataMapping) m;
                boolean drillDown = callback.onBeginMultipleMapping(classMapping, constantMetaDataMapping);

                if (drillDown) {
                    callback.onConstantMetaDataMappaing(classMapping, constantMetaDataMapping);
                    callback.onResourcePropertyMapping(constantMetaDataMapping);
                }

                callback.onEndMultiplMapping(classMapping, constantMetaDataMapping);
            } else if (m instanceof AbstractCollectionMapping) {
                // collection, add the internal element attributes
                AbstractCollectionMapping colMapping = (AbstractCollectionMapping) m;
                callback.onBeginCollectionMapping(colMapping);
                Mapping elementMapping = colMapping.getElementMapping();
                if (elementMapping instanceof ClassPropertyMapping) {
                    ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) elementMapping;
                    iteratePropertyMapping(callback, classMapping, classPropertyMapping);
                } else if (elementMapping instanceof ClassDynamicPropertyMapping) {
                    ClassDynamicPropertyMapping dynamicPropertyMapping = (ClassDynamicPropertyMapping) elementMapping;
                    callback.onClassDynamicPropertyMapping(classMapping, dynamicPropertyMapping);
                } else if (elementMapping instanceof ComponentMapping) {
                    ComponentMapping componentMapping = (ComponentMapping) elementMapping;
                    iterateComponentMapping(callback, classMapping, componentMapping, recursive);
                } else if (elementMapping instanceof ReferenceMapping) {
                    ReferenceMapping referenceMapping = (ReferenceMapping) elementMapping;
                    iterateReferenceMapping(callback, classMapping, referenceMapping, recursive);
                } else if (elementMapping instanceof PlainCascadeMapping) {
                    callback.onCascadeMapping(classMapping, (PlainCascadeMapping) elementMapping);
                }
                callback.onEndCollectionMapping(colMapping);
            }
        }
        callback.onEndClassMapping(classMapping);
    }

    private static void iterateReferenceMapping(ClassMappingCallback callback, ClassMapping classMapping,
                                                ReferenceMapping referenceMapping, boolean recursive) {
        boolean drillDown = callback.onBeginMultipleMapping(classMapping, referenceMapping);
        if (drillDown) {
            callback.onReferenceMapping(classMapping, referenceMapping);

            if (recursive) {
                ClassMapping[] refMappings = referenceMapping.getRefClassMappings();
                for (ClassMapping refMapping : refMappings) {
                    iterateMappings(callback, refMapping);
                }

                if (referenceMapping.getRefCompMapping() != null) {
                    iterateMappings(callback, referenceMapping.getRefCompMapping());
                }
            }
        }
        callback.onEndMultiplMapping(classMapping, referenceMapping);
    }

    private static void iterateComponentMapping(ClassMappingCallback callback, ClassMapping classMapping,
                                                ComponentMapping componentMapping, boolean recursive) {
        boolean drillDown = callback.onBeginMultipleMapping(classMapping, componentMapping);
        if (drillDown) {
            callback.onComponentMapping(classMapping, componentMapping);
            if (recursive) {
                ClassMapping[] refMappings = componentMapping.getRefClassMappings();
                for (ClassMapping refMapping : refMappings) {
                    iterateMappings(callback, refMapping);
                }
            }
        }
        callback.onEndMultiplMapping(classMapping, componentMapping);
    }

    private static void iteratePropertyMapping(ClassMappingCallback callback, ClassMapping classMapping,
                                               ClassPropertyMapping classPropertyMapping) {
        boolean drillDown = callback.onBeginMultipleMapping(classMapping, classPropertyMapping);
        if (drillDown) {
            callback.onClassPropertyMapping(classMapping, classPropertyMapping);
            for (Iterator resIt = classPropertyMapping.mappingsIt(); resIt.hasNext();) {
                ClassPropertyMetaDataMapping classPropertyMetaDataMapping = (ClassPropertyMetaDataMapping) resIt.next();
                callback.onClassPropertyMetaDataMapping(classPropertyMetaDataMapping);
                callback.onResourcePropertyMapping(classPropertyMetaDataMapping);
            }
        }
        callback.onEndMultiplMapping(classMapping, classPropertyMapping);
    }

}
