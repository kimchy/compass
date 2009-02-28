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

package org.compass.core.config.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.internal.InternalMapping;
import org.compass.core.mapping.osem.*;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.util.IdentityHashSet;
import org.compass.core.util.StringUtils;

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

    private LinkedList<String> prefixes = new LinkedList<String>();

    /**
     * Used to externaly control the managed id option (for example for ref-comp-mapping,
     * where we do not want internal ids being created, since we will never unmarshall it)
     */
    private ManagedId managedId = null;


    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.compassMapping = compassMapping;
        this.namingStrategy = namingStrategy;
        this.converterLookup = converterLookup;
        this.settings = settings;

        ArrayList<AliasMapping> mappings = new ArrayList<AliasMapping>();
        ((InternalCompassMapping) compassMapping).setPath(namingStrategy.getRootPath());
        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                clearRootClassMappingState();
                ClassMapping classMapping = (ClassMapping) aliasMapping;
                if (classMapping.isSupportUnmarshall()) {
                    classMapping = (ClassMapping) classMapping.copy();
                    secondPass(classMapping, compassMapping);
                } else {
//                    classMapping = (ClassMapping) classMapping.copy();
                    secondPassNoUnmarshalling(classMapping);
                }
                mappings.add(classMapping);
            } else {
                mappings.add(aliasMapping);
            }
        }
        ((InternalCompassMapping) compassMapping).clearMappings();
        for (AliasMapping aliasMapping : mappings) {
            ((InternalCompassMapping) compassMapping).addMapping(aliasMapping);
        }

        return compassMapping;
    }

    private void secondPassNoUnmarshalling(ClassMapping classMapping) {
        classMapping.setPath(namingStrategy.buildPath(compassMapping.getPath(), classMapping.getAlias()));
        classMapping.setClassPath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_CLASS).hintStatic());
        classMapping.setEnumNamePath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_ENUM_NAME).hintStatic());
        OsemMappingIterator.iterateMappings(new NoUnmarshallingCallback(classMapping), classMapping, true);
    }

    private void secondPass(ClassMapping classMapping, CompassMapping fatherMapping) {
        classMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classMapping.getAlias()));
        secondPass(classMapping, false, true);
    }

    private void secondPass(ClassMapping classMapping, boolean onlyProperties, boolean topmost) {
        classMapping.setClassPath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_CLASS).hintStatic());
        classMapping.setEnumNamePath(namingStrategy.buildPath(classMapping.getPath(), MarshallingEnvironment.PROPERTY_ENUM_NAME).hintStatic());
        ArrayList<Mapping> innerMappingsCopy = new ArrayList<Mapping>();
        for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            Mapping copyMapping = m.copy();
            boolean removeMapping = false;

            if ((copyMapping instanceof ObjectMapping) && topmost) {
                PropertyPath aliasedPath = namingStrategy.buildPath(compassMapping.getPath(), ((ObjectMapping) copyMapping).getDefinedInAlias());
                ((InternalMapping) copyMapping).setPath(namingStrategy.buildPath(aliasedPath, copyMapping.getName()));
            } else {
                ((InternalMapping) copyMapping).setPath(namingStrategy.buildPath(classMapping.getPath(), copyMapping.getName()));
            }

            if (copyMapping instanceof ClassPropertyMapping) {
                removeMapping = secondPass((ClassPropertyMapping) copyMapping, classMapping);
            } else if (copyMapping instanceof IdComponentMapping) {
                removeMapping = secondPass((IdComponentMapping) copyMapping, classMapping);
            } else {
                if (!onlyProperties) {
                    if (copyMapping instanceof ClassDynamicPropertyMapping) {
                        removeMapping = secondPass((ClassDynamicPropertyMapping) copyMapping);
                    } else if (copyMapping instanceof ComponentMapping) {
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
                        removeMapping = secondPass((DynamicMetaDataMapping) copyMapping);
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
        ((InternalMapping) elementMappingCopy).setPath(collectionMapping.getPath());

        boolean removeMapping = false;
        if (elementMappingCopy instanceof ClassPropertyMapping) {
            removeMapping = secondPass((ClassPropertyMapping) elementMappingCopy, fatherMapping);
        } else if (elementMappingCopy instanceof ComponentMapping) {
            removeMapping = secondPass((ComponentMapping) elementMappingCopy, fatherMapping);
        } else if (elementMappingCopy instanceof ReferenceMapping) {
            removeMapping = secondPass((ReferenceMapping) elementMappingCopy, fatherMapping);
        }

        collectionMapping.setElementMapping(elementMappingCopy);

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
            managedId = ManagedId.FALSE;
            secondPass(refClass, false, false);
            managedId = null;
            refClass.setRoot(false);

            referenceMapping.setRefCompMapping(refClass);
        }

        return false;
    }

    private void secondPassJustReference(ReferenceMapping referenceMapping, Mapping fatherMapping) {
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
            secondPass(refClass, true, false);

            for (ClassIdPropertyMapping mapping : refClass.findClassPropertyIdMappings()) {
                mapping.clearMappings();
                // create the internal id
                MappingProcessorUtils.addInternalId(settings, converterLookup, mapping, true);
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

        if (compMapping.getPrefix() != null) {
            prefixes.add(compMapping.getPrefix());
        }

        ClassMapping[] refClassMappings = compMapping.getRefClassMappings();
        ClassMapping[] copyRefClassMappings = new ClassMapping[refClassMappings.length];
        for (int i = 0; i < refClassMappings.length; i++) {
            ClassMapping refClassMapping = (ClassMapping) refClassMappings[i].copy();
            refClassMapping.setPath(compMapping.getPath());
            secondPass(refClassMapping, false, false);
            refClassMapping.setRoot(false);
            copyRefClassMappings[i] = refClassMapping;
        }
        compMapping.setRefClassMappings(copyRefClassMappings);

        chainedComponents.remove(compMapping);

        if (compMapping.getPrefix() != null) {
            prefixes.removeLast();
        }

        return false;
    }

    private boolean secondPass(ClassPropertyMapping classPropertyMapping, Mapping fatherMapping) {
        // we check if we override the managedId option
        if (managedId != null) {
            classPropertyMapping.setManagedId(managedId);
        }

        ArrayList<ClassPropertyMetaDataMapping> innerMappingsCopy = new ArrayList<ClassPropertyMetaDataMapping>();
        for (Iterator it = classPropertyMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            ClassPropertyMetaDataMapping metaDataMappingCopy = (ClassPropertyMetaDataMapping) m.copy();
            metaDataMappingCopy.setName(buildFullName(metaDataMappingCopy.getOriginalName()));
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
        constantMapping.setName(buildFullName(constantMapping.getOriginalName()));
        constantMapping.setPath(new StaticPropertyPath(constantMapping.getName()));
        return false;
    }

    private boolean secondPass(DynamicMetaDataMapping dynamicMetaDataMapping) {
        dynamicMetaDataMapping.setName(buildFullName(dynamicMetaDataMapping.getOriginalName()));
        dynamicMetaDataMapping.setPath(new StaticPropertyPath(dynamicMetaDataMapping.getName()));
        return false;
    }

    private boolean secondPass(ClassDynamicPropertyMapping dynamicPropertyMapping) {
        dynamicPropertyMapping.setNamePrefix(buildDynamicNamePrefix(dynamicPropertyMapping.getNamePrefix()));
        return false;
    }

    private void clearRootClassMappingState() {
        chainedComponents.clear();
        prefixes.clear();
    }

    private String buildDynamicNamePrefix(String namePrefix) {
        if (namePrefix == null) {
            namePrefix = buildFullName("");
        } else {
            namePrefix = buildFullName(namePrefix);
        }
        if (StringUtils.hasText(namePrefix)) {
            return namePrefix;
        }
        return null;
    }

    private String buildFullName(String name) {
        if (prefixes.isEmpty()) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixes) {
            sb.append(prefix);
        }
        sb.append(name);
        return sb.toString();
    }

    private class NoUnmarshallingCallback implements OsemMappingIterator.ClassMappingCallback {

        private ClassMapping rootClassMapping;

        private IdentityHashSet<ClassMapping> idComponents = new IdentityHashSet<ClassMapping>();

        private Set<String> cyclicClassMappings = new HashSet<String>();

        private ClassPropertyMapping classPropertyMapping;

        private NoUnmarshallingCallback(ClassMapping rootClassMapping) {
            this.rootClassMapping = rootClassMapping;
        }

        /**
         * In case we do not need to support unmarshalling, we need to perform simple cyclic detection
         * and return <code>false</code> (won't iterate into this class mapping) if we already passed
         * this class mapping. We will remove the marker in the {@link #onEndClassMapping(ClassMapping)}.
         */
        public boolean onBeginClassMapping(ClassMapping classMapping) {
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

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            if ((mapping instanceof ReferenceMapping) && classMapping != rootClassMapping) {
                return false;
            }
            if (mapping instanceof ComponentMapping) {
                ComponentMapping componentMapping = (ComponentMapping) mapping;
                if (componentMapping.getPrefix() != null) {
                    prefixes.add(componentMapping.getPrefix());
                }
            }
            return true;
        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
            if (mapping instanceof ComponentMapping) {
                ComponentMapping componentMapping = (ComponentMapping) mapping;
                if (componentMapping.getPrefix() != null) {
                    prefixes.removeLast();
                }
            }
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping) {
            this.classPropertyMapping = classPropertyMapping;

            if (classMapping == rootClassMapping) {
                // only apply the path for class properties that belong to the class mapping we started with
                // and not onces we iterate through recuresivly. This is because then we override (by mistake)
                // intenral ids of other class mappings.
                PropertyPath aliasedPath = namingStrategy.buildPath(compassMapping.getPath(), classPropertyMapping.getDefinedInAlias());
                classPropertyMapping.setPath(namingStrategy.buildPath(aliasedPath, classPropertyMapping.getName()));
            } else if (idComponents.contains(classMapping)) {
                classPropertyMapping.setPath(namingStrategy.buildPath(classMapping.getPath(), classPropertyMapping.getName()));
            }
        }

        public void onClassDynamicPropertyMapping(ClassMapping classMapping, ClassDynamicPropertyMapping dynamicPropertyMapping) {
            if (classMapping == rootClassMapping) {
                PropertyPath aliasedPath = namingStrategy.buildPath(compassMapping.getPath(), dynamicPropertyMapping.getDefinedInAlias());
                dynamicPropertyMapping.setPath(namingStrategy.buildPath(aliasedPath, dynamicPropertyMapping.getName()));
            } else {
                dynamicPropertyMapping.setPath(namingStrategy.buildPath(classMapping.getPath(), dynamicPropertyMapping.getName()));
            }
            dynamicPropertyMapping.setNamePrefix(buildDynamicNamePrefix(dynamicPropertyMapping.getNamePrefix()));
        }

        public void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping) {
        }

        public void onCascadeMapping(ClassMapping classMapping, PlainCascadeMapping cascadeMapping) {
        }

        public void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping) {
            ClassMapping[] refClassMappings = componentMapping.getRefClassMappings();
            ClassMapping[] copyRefClassMappings = new ClassMapping[refClassMappings.length];
            for (int i = 0; i < refClassMappings.length; i++) {
                ClassMapping refClassMapping;
                if (componentMapping.getPrefix() != null) {
                    refClassMapping = (ClassMapping) refClassMappings[i].copy();
                    refClassMapping.setPath(componentMapping.getPath());
                    refClassMapping.setSupportUnmarshall(classMapping.isSupportUnmarshall());
                } else if (componentMapping instanceof IdComponentMapping && (classMapping == rootClassMapping)) {
                    refClassMapping = (ClassMapping) refClassMappings[i].copy();
                    PropertyPath aliasedPath = namingStrategy.buildPath(compassMapping.getPath(), componentMapping.getDefinedInAlias());
                    refClassMapping.setPath(namingStrategy.buildPath(aliasedPath, componentMapping.getName()));
                    idComponents.add(refClassMapping);
                    // We set here that we support unmarshall since we need to marshall the internal ids of the
                    // id component
                    refClassMapping.setSupportUnmarshall(true);
                } else {
                    // perform a shalow copy, and copy over the child mappings
                    refClassMapping = (ClassMapping) refClassMappings[i].shallowCopy();
                    refClassMapping.replaceMappings(refClassMappings[i]);
                    refClassMapping.setPath(componentMapping.getPath());
                    refClassMapping.setSupportUnmarshall(classMapping.isSupportUnmarshall());
                }

                refClassMapping.setRoot(false);
                copyRefClassMappings[i] = refClassMapping;
            }
            componentMapping.setRefClassMappings(copyRefClassMappings);
        }

        public void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping) {
            // TODO why do we even process refernece mapping when we don't support unmarshall?
            PropertyPath aliasedPath = namingStrategy.buildPath(compassMapping.getPath(), referenceMapping.getDefinedInAlias());
            referenceMapping.setPath(namingStrategy.buildPath(aliasedPath, referenceMapping.getName()));
            secondPassJustReference(referenceMapping, classMapping);
        }

        public void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping) {
            constantMetaDataMapping.setName(buildFullName(constantMetaDataMapping.getOriginalName()));
            constantMetaDataMapping.setPath(new StaticPropertyPath(constantMetaDataMapping.getName()));
        }


        public void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping) {
            dynamicMetaDataMapping.setName(buildFullName(dynamicMetaDataMapping.getOriginalName()));
            dynamicMetaDataMapping.setPath(new StaticPropertyPath(dynamicMetaDataMapping.getName()));
        }

        public void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping) {
            classPropertyMetaDataMapping.setName(buildFullName(classPropertyMetaDataMapping.getOriginalName()));
            MappingProcessorUtils.process(classPropertyMetaDataMapping, classPropertyMapping, converterLookup);
        }

        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
        }
    }
}
