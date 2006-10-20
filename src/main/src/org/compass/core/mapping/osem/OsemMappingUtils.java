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

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.Assert;

/**
 * @author kimchy
 */
public abstract class OsemMappingUtils {

    public static interface ClassMappingCallback {

        void onBeginClassMapping(ClassMapping classMapping);

        void onEndClassMapping(ClassMapping classMapping);

        boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping);

        void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping);

        void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onEndCollectionMapping(AbstractCollectionMapping collectionMapping);

        void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping);

        void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping);

        void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping);

        void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping);

        void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping);

        void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping);

        void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping);

        void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping);
    }

    public static class ClassPropertyAndResourcePropertyGatherer implements ClassMappingCallback {

        private ArrayList classPropertyMappings = new ArrayList();

        private ArrayList resourcePropertyMappings = new ArrayList();

        private HashMap ignoreInheritedDuplicatesClassMappings = new HashMap();

        public ClassPropertyAndResourcePropertyGatherer() {

        }

        public ArrayList getClassPropertyMappings() {
            return classPropertyMappings;
        }

        public ArrayList getResourcePropertyMappings() {
            return resourcePropertyMappings;
        }

        public void onBeginClassMapping(ClassMapping classMapping) {
        }

        public void onEndClassMapping(ClassMapping classMapping) {
        }

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            // if it is ref alias mapping, mark its multiple ref class mappings as ones
            // to be considered for ignoring inherited duplicates
            if (mapping instanceof HasRefAliasMapping) {
                ClassMapping[] classMappings = ((HasRefAliasMapping) mapping).getRefClassMappings();
                if (classMappings.length > 1) {
                    HashMap byAlias = new HashMap();
                    for (int i = 0; i < classMappings.length; i++) {
                        ignoreInheritedDuplicatesClassMappings.put(new Integer(System.identityHashCode(classMappings[i])), byAlias);
                    }
                }
            }
            // check if the class mapping is in the inherited duplicates checking
            // if so, only traverse the first one, and the rest of the duplicates
            // just ignore by returning false for drilling down
            HashMap byAlias = (HashMap) ignoreInheritedDuplicatesClassMappings.get(new Integer(System.identityHashCode(classMapping)));
            if (byAlias != null && (mapping instanceof ObjectMapping)) {
                ObjectMapping objectMapping = (ObjectMapping) mapping;
                Assert.notNull(objectMapping.getDefinedInAlias(), "Internal Compass Error, Defined in Alias not found for [" +
                        objectMapping.getPropertyName() + "] in alias [" + classMapping.getAlias() + "]");
                HashMap propByAlias = (HashMap) byAlias.get(objectMapping.getDefinedInAlias());
                if (propByAlias == null) {
                    propByAlias = new HashMap();
                    byAlias.put(objectMapping.getDefinedInAlias(), propByAlias);
                }
                ObjectMapping actualObjectMapping = (ObjectMapping) propByAlias.get(objectMapping.getPropertyName());
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

        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
            resourcePropertyMappings.add(resourcePropertyMapping);
        }

    }

    public static void iterateMappings(ClassMappingCallback callback, ClassMapping classMapping) {
        iterateMappings(callback, classMapping, true);
    }

    public static void iterateMappings(ClassMappingCallback callback, ClassMapping classMapping, boolean recursive) {
        callback.onBeginClassMapping(classMapping);
        for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
            Mapping m = (Mapping) mappingsIt.next();
            if (m instanceof ClassPropertyMapping) {
                ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) m;
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
            } else if (m instanceof ParentMapping) {
                callback.onParentMapping(classMapping, (ParentMapping) m);
            } else if (m instanceof DynamicMetaDataMapping) {
                DynamicMetaDataMapping dynamicMetaDataMapping = (DynamicMetaDataMapping) m;
                callback.onDynamicMetaDataMapping(classMapping, dynamicMetaDataMapping);
                callback.onResourcePropertyMapping(dynamicMetaDataMapping);
            } else if (m instanceof ComponentMapping) {
                ComponentMapping componentMapping = (ComponentMapping) m;
                callback.onBeginMultipleMapping(classMapping, componentMapping);

                callback.onComponentMapping(classMapping, componentMapping);
                if (recursive) {
                    ClassMapping[] refMappings = componentMapping.getRefClassMappings();
                    for (int i = 0; i < refMappings.length; i++) {
                        OsemMappingUtils.iterateMappings(callback, refMappings[i]);
                    }
                }

                callback.onEndMultiplMapping(classMapping, componentMapping);
            } else if (m instanceof ReferenceMapping) {
                ReferenceMapping referenceMapping = (ReferenceMapping) m;
                callback.onBeginMultipleMapping(classMapping, referenceMapping);

                callback.onReferenceMapping(classMapping, referenceMapping);

                if (recursive) {
                    ClassMapping[] refMappings = referenceMapping.getRefClassMappings();
                    for (int i = 0; i < refMappings.length; i++) {
                        OsemMappingUtils.iterateMappings(callback, refMappings[i]);
                    }

                    if (referenceMapping.getRefCompMapping() != null) {
                        OsemMappingUtils.iterateMappings(callback, referenceMapping.getRefCompMapping());
                    }
                }

                callback.onEndMultiplMapping(classMapping, referenceMapping);
            } else if (m instanceof ConstantMetaDataMapping) {
                ConstantMetaDataMapping constantMetaDataMapping = (ConstantMetaDataMapping) m;
                callback.onBeginMultipleMapping(classMapping, constantMetaDataMapping);

                callback.onConstantMetaDataMappaing(classMapping, constantMetaDataMapping);
                callback.onResourcePropertyMapping(constantMetaDataMapping);

                callback.onEndMultiplMapping(classMapping, constantMetaDataMapping);
            } else if (m instanceof AbstractCollectionMapping) {
                // collection, add the internal element attributes
                AbstractCollectionMapping colMapping = (AbstractCollectionMapping) m;
                callback.onBeginCollectionMapping(colMapping);
                Mapping elementMapping = colMapping.getElementMapping();
                if (elementMapping instanceof ClassPropertyMapping) {
                    ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) elementMapping;
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
                } else if (elementMapping instanceof ComponentMapping) {
                    ComponentMapping componentMapping = (ComponentMapping) elementMapping;
                    callback.onBeginMultipleMapping(classMapping, componentMapping);

                    callback.onComponentMapping(classMapping, componentMapping);

                    if (recursive) {
                        ClassMapping[] refMappings = componentMapping.getRefClassMappings();
                        for (int i = 0; i < refMappings.length; i++) {
                            OsemMappingUtils.iterateMappings(callback, refMappings[i]);
                        }
                    }

                    callback.onEndMultiplMapping(classMapping, componentMapping);
                } else if (elementMapping instanceof ReferenceMapping) {
                    ReferenceMapping referenceMapping = (ReferenceMapping) elementMapping;
                    callback.onBeginMultipleMapping(classMapping, referenceMapping);

                    callback.onReferenceMapping(classMapping, referenceMapping);

                    if (recursive) {
                        ClassMapping[] refMappings = referenceMapping.getRefClassMappings();
                        for (int i = 0; i < refMappings.length; i++) {
                            OsemMappingUtils.iterateMappings(callback, refMappings[i]);
                        }

                        if (referenceMapping.getRefCompMapping() != null) {
                            OsemMappingUtils.iterateMappings(callback, referenceMapping.getRefCompMapping());
                        }
                    }

                    callback.onEndMultiplMapping(classMapping, referenceMapping);
                }
                callback.onEndCollectionMapping(colMapping);
            }
        }
        callback.onEndClassMapping(classMapping);
    }

}
