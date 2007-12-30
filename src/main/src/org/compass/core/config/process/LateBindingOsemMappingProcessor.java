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

package org.compass.core.config.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.IdComponentMapping;
import org.compass.core.mapping.osem.OsemMappingIterator;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class LateBindingOsemMappingProcessor implements MappingProcessor {

    private CompassMapping compassMapping;

    private PropertyNamingStrategy namingStrategy;

    private ConverterLookup converterLookup;

    private CompassSettings settings;

    // helper runtime state

    private List<ComponentMapping> chainedComponents = new ArrayList<ComponentMapping>();

    /**
     * Used to externaly control the managed id option (for example for ref-comp-mapping,
     * where we do not want internal ids being created, since we will never unmarshall it)
     */
    private ClassPropertyMapping.ManagedId managedId = null;


    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.compassMapping = compassMapping;
        this.namingStrategy = namingStrategy;
        this.converterLookup = converterLookup;
        this.settings = settings;

        compassMapping.setPath(namingStrategy.getRootPath());
        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassMapping) {
                clearRootClassMappingState();
                ClassMapping classMapping = (ClassMapping) m;
                if (classMapping.isSupportUnmarshall()) {
                    secondPass(classMapping, compassMapping);
                } else {
                    secondPassNoUnmarshalling(classMapping);
                }
            }
        }

        return compassMapping;
    }

    private void secondPassNoUnmarshalling(ClassMapping classMapping) {
        classMapping.setPath(namingStrategy.buildPath(compassMapping.getPath(), classMapping.getAlias()));
        classMapping.setClassPath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_CLASS).hintStatic());
        OsemMappingIterator.iterateMappings(new NoUnmarshallingCallback(classMapping), classMapping, false);
    }

    private void secondPass(ClassMapping classMapping, CompassMapping fatherMapping) {
        classMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classMapping.getAlias()));
        secondPass(classMapping, false);
    }

    private void secondPass(ClassMapping classMapping, boolean onlyProperties) {
        classMapping.setClassPath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_CLASS).hintStatic());
        ArrayList<Mapping> innerMappingsCopy = new ArrayList<Mapping>();
        for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            Mapping copyMapping = m.copy();
            boolean removeMapping = false;
            if (m instanceof ClassPropertyMapping) {
                removeMapping = secondPass((ClassPropertyMapping) copyMapping, classMapping);
            } else if (m instanceof IdComponentMapping) {
                removeMapping = secondPass((IdComponentMapping) copyMapping, classMapping);
            } else {
                if (!onlyProperties) {
                    if (copyMapping instanceof ComponentMapping) {
                        removeMapping = secondPass((ComponentMapping) copyMapping, classMapping);
                    } else if (copyMapping instanceof ReferenceMapping) {
                        removeMapping = secondPass((ReferenceMapping) copyMapping, classMapping);
                    } else if (copyMapping instanceof ConstantMetaDataMapping) {
                        removeMapping = secondPass((ConstantMetaDataMapping) copyMapping);
                    } else if (copyMapping instanceof ParentMapping) {
                        // nothing to do here
                    } else if (copyMapping instanceof AbstractCollectionMapping) {
                        removeMapping = secondPass((AbstractCollectionMapping) copyMapping, classMapping);
                    } else if (copyMapping instanceof DynamicMetaDataMapping) {
                    }
                }
            }
            if (!removeMapping) {
                innerMappingsCopy.add(copyMapping);
            }
        }
        classMapping.clearMappings();
        for (Iterator<Mapping> it = innerMappingsCopy.iterator(); it.hasNext();) {
            classMapping.addMapping(it.next());
        }
    }

    private boolean secondPass(AbstractCollectionMapping collectionMapping, Mapping fatherMapping) {
        Mapping elementMapping = collectionMapping.getElementMapping();
        Mapping elementMappingCopy = elementMapping.copy();
        boolean removeMapping = false;
        if (elementMappingCopy instanceof ClassPropertyMapping) {
            removeMapping = secondPass((ClassPropertyMapping) elementMappingCopy, fatherMapping);
        } else if (elementMappingCopy instanceof ComponentMapping) {
            removeMapping = secondPass((ComponentMapping) elementMappingCopy, fatherMapping);
        } else if (elementMappingCopy instanceof ReferenceMapping) {
            removeMapping = secondPass((ReferenceMapping) elementMappingCopy, fatherMapping);
        }

        collectionMapping.setElementMapping(elementMappingCopy);

        collectionMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), collectionMapping.getName()));
        collectionMapping.setCollectionTypePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_TYPE).hintStatic());
        collectionMapping.setColSizePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_SIZE).hintStatic());

        return removeMapping;
    }

    private boolean secondPass(ReferenceMapping referenceMapping, Mapping fatherMapping) {
        secondPassJustReference(referenceMapping, fatherMapping);

        // now configure the component mapping if exists
        if (referenceMapping.getRefCompAlias() != null) {
            ClassMapping pointerClass = (ClassMapping) compassMapping.getMappingByAlias(referenceMapping
                    .getRefCompAlias());
            if (pointerClass == null) {
                throw new MappingException("Failed to locate mapping for reference ref-comp-alias ["
                        + referenceMapping.getRefCompAlias() + "]");
            }

            ClassMapping refClass = (ClassMapping) pointerClass.copy();
            refClass.setPath(namingStrategy.buildPath(referenceMapping.getPath(), referenceMapping.getRefCompAlias()));
            // we do not want to create intenral ids, since we will never unmarshall it
            managedId = ClassPropertyMapping.ManagedId.FALSE;
            secondPass(refClass, false);
            managedId = null;
            refClass.setRoot(false);

            referenceMapping.setRefCompMapping(refClass);
        }

        return false;
    }

    private void secondPassJustReference(ReferenceMapping referenceMapping, Mapping fatherMapping) {
        referenceMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), referenceMapping.getName()));
        ClassMapping[] refMappings = referenceMapping.getRefClassMappings();

        ClassMapping[] copyRefClassMappings = new ClassMapping[refMappings.length];
        for (int i = 0; i < refMappings.length; i++) {
            // in case of reference, use internal ids for the refrence ids
            // get the original ids, since we will copy them later
            List<Mapping> ids = refMappings[i].findIdMappings();

            // shallow copy the ref class mappings, and ony add the ids (as copies)
            ClassMapping refClass = (ClassMapping) refMappings[i].shallowCopy();
            for (Object id : ids) {
                refClass.addMapping(((Mapping) id).copy());
            }

            refClass.setPath(referenceMapping.getPath());
            secondPass(refClass, true);

            for (ClassIdPropertyMapping mapping : refClass.findClassPropertyIdMappings()) {
                mapping.clearMappings();
                // create the internal id
                MappingProcessorUtils.addInternalId(settings, converterLookup, mapping);
            }
            // since we create our own special ref class mapping that only holds the
            // ids, we need to call the post process here
            refClass.postProcess();
            copyRefClassMappings[i] = refClass;
        }
        referenceMapping.setRefClassMappings(copyRefClassMappings);
    }

    private boolean secondPass(ComponentMapping compMapping, Mapping fatherMapping) {
        int numberOfComponentsWithTheSameAlias = 0;
        for (Iterator<ComponentMapping> it = chainedComponents.iterator(); it.hasNext();) {
            ComponentMapping tempComponentMapping = it.next();
            if (compMapping.hasAtLeastOneRefAlias(tempComponentMapping.getRefAliases())) {
                numberOfComponentsWithTheSameAlias++;
            }
        }
        if (numberOfComponentsWithTheSameAlias >= compMapping.getMaxDepth()) {
            return true;
        }

        chainedComponents.add(compMapping);

        compMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), compMapping.getName()));

        ClassMapping[] refClassMappings = compMapping.getRefClassMappings();
        ClassMapping[] copyRefClassMappings = new ClassMapping[refClassMappings.length];
        for (int i = 0; i < refClassMappings.length; i++) {
            ClassMapping refClassMapping = (ClassMapping) refClassMappings[i].copy();
            refClassMapping.setPath(compMapping.getPath());
            secondPass(refClassMapping, false);
            refClassMapping.setRoot(false);
            copyRefClassMappings[i] = refClassMapping;
        }
        compMapping.setRefClassMappings(copyRefClassMappings);

        chainedComponents.remove(compMapping);

        return false;
    }

    private boolean secondPass(ClassPropertyMapping classPropertyMapping, Mapping fatherMapping) {
        classPropertyMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classPropertyMapping.getName()));

        // we check if we override the managedId option
        if (managedId != null) {
            classPropertyMapping.setManagedId(managedId);
        }

        ArrayList<ClassPropertyMetaDataMapping> innerMappingsCopy = new ArrayList<ClassPropertyMetaDataMapping>();
        for (Iterator it = classPropertyMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            ClassPropertyMetaDataMapping metaDataMappingCopy = (ClassPropertyMetaDataMapping) m.copy();
            MappingProcessorUtils.process(metaDataMappingCopy, classPropertyMapping, converterLookup);
            innerMappingsCopy.add(metaDataMappingCopy);
        }
        classPropertyMapping.clearMappings();
        for (Iterator<ClassPropertyMetaDataMapping> it = innerMappingsCopy.iterator(); it.hasNext();) {
            classPropertyMapping.addMapping(it.next());
        }
        return false;
    }

    private boolean secondPass(ConstantMetaDataMapping constantMapping) {
        constantMapping.setPath(new StaticPropertyPath(constantMapping.getName()));
        return false;
    }

    private void clearRootClassMappingState() {
        chainedComponents.clear();
    }

    private class NoUnmarshallingCallback implements OsemMappingIterator.ClassMappingCallback {

        private ClassPropertyMapping classPropertyMapping;

        private ClassMapping classMapping;

        public NoUnmarshallingCallback(ClassMapping classMapping) {
            this.classMapping = classMapping;
        }

        public void onBeginClassMapping(ClassMapping classMapping) {
        }

        public void onEndClassMapping(ClassMapping classMapping) {
        }

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            return true;
        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping) {
            this.classPropertyMapping = classPropertyMapping;
            classPropertyMapping.setPath(namingStrategy.buildPath(classMapping.getPath(), classPropertyMapping.getName()));
        }

        public void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping) {
        }

        public void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping) {
            ClassMapping[] refClassMappings = componentMapping.getRefClassMappings();
            ClassMapping[] copyRefClassMappings = new ClassMapping[refClassMappings.length];
            for (int i = 0; i < refClassMappings.length; i++) {
                // perform a shalow copy, and copy over the child mappings
                ClassMapping refClassMapping = (ClassMapping) refClassMappings[i].shallowCopy();
                refClassMapping.replaceMappings(refClassMappings[i]);

                refClassMapping.setPath(componentMapping.getPath());
                refClassMapping.setRoot(false);
                refClassMapping.setSupportUnmarshall(classMapping.isSupportUnmarshall());
                copyRefClassMappings[i] = refClassMapping;
            }
            componentMapping.setRefClassMappings(copyRefClassMappings);
        }

        public void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping) {
            secondPassJustReference(referenceMapping, classMapping);
        }

        public void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping) {
            constantMetaDataMapping.setPath(new StaticPropertyPath(constantMetaDataMapping.getName()));
        }


        public void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping) {
        }

        public void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping) {
            MappingProcessorUtils.process(classPropertyMetaDataMapping, classPropertyMapping, converterLookup);
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
        }
    }
}
