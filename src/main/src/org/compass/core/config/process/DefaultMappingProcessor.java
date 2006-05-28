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
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.osem.*;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class DefaultMappingProcessor implements MappingProcessor {

    private CompassMapping compassMapping;

    private PropertyNamingStrategy namingStrategy;

    private ConverterLookup converterLookup;

    private CompassSettings settings;

    // helper runtime state

    private List chainedComponents = new ArrayList();

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
                secondPass((ClassMapping) m, compassMapping);
            } else if (m instanceof RawResourceMapping) {
                secondPass((RawResourceMapping) m);
            }
        }

        return compassMapping;
    }

    private void secondPass(RawResourceMapping resourceMapping) {
        secondPassConverter(resourceMapping, true);
        for (Iterator it = resourceMapping.mappingsIt(); it.hasNext();) {
            secondPassConverter((Mapping) it.next(), false);
        }
    }

    private void secondPass(ClassMapping classMapping, CompassMapping fatherMapping) {
        classMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classMapping.getAlias()));
        secondPass(classMapping, false);
    }

    private void secondPass(ClassMapping classMapping, boolean onlyProperties) {
        classMapping.setClassPath(namingStrategy.buildPath(classMapping.getPath(),
                MarshallingEnvironment.PROPERTY_CLASS));
        secondPassConverter(classMapping);
        ArrayList innerMappingsCopy = new ArrayList();
        for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            Mapping copyMapping = m.copy();
            boolean removeMapping = false;
            if (m instanceof ClassPropertyMapping) {
                removeMapping = secondPass((ClassPropertyMapping) copyMapping, classMapping);
            } else {
                if (!onlyProperties) {
                    if (copyMapping instanceof ComponentMapping) {
                        removeMapping = secondPass((ComponentMapping) copyMapping, classMapping);
                    } else if (copyMapping instanceof ReferenceMapping) {
                        removeMapping = secondPass((ReferenceMapping) copyMapping, classMapping);
                    } else if (copyMapping instanceof ConstantMetaDataMapping) {
                        removeMapping = secondPass((ConstantMetaDataMapping) copyMapping);
                    } else if (copyMapping instanceof ParentMapping) {
                        removeMapping = secondPass((ParentMapping) copyMapping);
                    } else if (copyMapping instanceof AbstractCollectionMapping) {
                        removeMapping = secondPass((AbstractCollectionMapping) copyMapping, classMapping);
                    }
                }
            }
            if (!removeMapping) {
                innerMappingsCopy.add(copyMapping);
            }
        }
        classMapping.clearMappings();
        for (Iterator it = innerMappingsCopy.iterator(); it.hasNext();) {
            classMapping.addMapping((Mapping) it.next());
        }
    }

    private boolean secondPass(AbstractCollectionMapping collectionMapping, Mapping fatherMapping) {
        secondPassConverter(collectionMapping);
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

        collectionMapping.setPath(elementMappingCopy.getPath());
        collectionMapping.setCollectionTypePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_TYPE));
        collectionMapping.setColSizePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_SIZE));

        return removeMapping;
    }

    private boolean secondPass(ReferenceMapping referenceMapping, Mapping fatherMapping) {
        referenceMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), referenceMapping.getName()));
        secondPassConverter(referenceMapping);
        ClassMapping pointerClass = referenceMapping.getRefClassMapping();

        if (pointerClass == null) {
            throw new MappingException("Failed to locate mapping for reference ref-alias ["
                    + referenceMapping.getRefAlias() + "]");
        }

        ClassMapping refClass = (ClassMapping) pointerClass.copy();
        refClass.setPath(referenceMapping.getPath());
        secondPass(refClass, true);

        // in case of reference, use internal ids for the refrence ids
        List ids = refClass.findClassPropertyIdMappings();
        // after we got the ids, we can clear the mappings from the ref class
        // mapping and add only internal ids
        refClass.clearMappings();
        for (Iterator it = ids.iterator(); it.hasNext();) {
            ClassIdPropertyMapping idMapping = (ClassIdPropertyMapping) it.next();
            idMapping.clearMappings();
            // create the internal id
            MappingProcessorUtils.addInternalId(settings, converterLookup, idMapping);
            // re-add it to the ref class mapping
            refClass.addMapping(idMapping);
        }
        // since we create our own special ref class mapping that only holds the
        // ids, we need to call the post process here
        refClass.postProcess();

        referenceMapping.setRefClassMapping(refClass);

        // now configure the component mapping if exists
        if (referenceMapping.getRefCompAlias() != null) {
            pointerClass = (ClassMapping) compassMapping.getResourceMappingByAlias(referenceMapping
                    .getRefCompAlias());
            if (pointerClass == null) {
                throw new MappingException("Failed to locate mapping for reference ref-comp-alias ["
                        + referenceMapping.getRefCompAlias() + "]");
            }

            refClass = (ClassMapping) pointerClass.copy();
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

    private boolean secondPass(ComponentMapping compMapping, Mapping fatherMapping) {
        int numberOfComponentsWithTheSameAlias = 0;
        for (Iterator it = chainedComponents.iterator(); it.hasNext();) {
            ComponentMapping tempComponentMapping = (ComponentMapping) it.next();
            if (compMapping.getRefAlias().equals(tempComponentMapping.getRefAlias())) {
                numberOfComponentsWithTheSameAlias++;
            }
        }
        if (numberOfComponentsWithTheSameAlias >= compMapping.getMaxDepth()) {
            return true;
        }

        chainedComponents.add(compMapping);

        compMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), compMapping.getName()));
        secondPassConverter(compMapping);
        ClassMapping refClassMapping = compMapping.getRefClassMapping();

        refClassMapping = (ClassMapping) refClassMapping.copy();
        refClassMapping.setPath(compMapping.getPath());
        secondPass(refClassMapping, false);
        refClassMapping.setRoot(false);

        compMapping.setRefClassMapping(refClassMapping);

        chainedComponents.remove(compMapping);

        return false;
    }

    private boolean secondPass(ParentMapping parentMapping) {
        secondPassConverter(parentMapping);
        return false;
    }

    private boolean secondPass(ClassPropertyMapping classPropertyMapping, Mapping fatherMapping) {
        secondPassConverter(classPropertyMapping);
        classPropertyMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classPropertyMapping.getName()));

        // we check is we override the managedId option
        if (managedId != null) {
            classPropertyMapping.setManagedId(managedId);
        }

        ArrayList innerMappingsCopy = new ArrayList();
        for (Iterator it = classPropertyMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            ClassPropertyMetaDataMapping metaDataMappingCopy = (ClassPropertyMetaDataMapping) m.copy();
            MappingProcessorUtils.process(metaDataMappingCopy, classPropertyMapping, converterLookup);
            innerMappingsCopy.add(metaDataMappingCopy);
        }
        classPropertyMapping.clearMappings();
        for (Iterator it = innerMappingsCopy.iterator(); it.hasNext();) {
            classPropertyMapping.addMapping((Mapping) it.next());
        }
        return false;
    }

    private boolean secondPass(ConstantMetaDataMapping constantMapping) {
        secondPassConverter(constantMapping);
        constantMapping.setPath(constantMapping.getName());
        return false;
    }

    private void secondPassConverter(Mapping mapping) {
        secondPassConverter(mapping, true);
    }

    private void secondPassConverter(Mapping mapping, boolean forceConverter) {
        if (mapping.getConverter() == null) {
            if (mapping.getConverterName() != null) {
                String converterName = mapping.getConverterName();
                mapping.setConverter(converterLookup.lookupConverter(converterName));
                if (mapping.getConverter() == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter [" + converterName + "] for mapping " +
                            "[" + mapping.getName() + "]");
                }
            } else {
                mapping.setConverter(converterLookup.lookupConverter(mapping.getClass()));
                if (mapping.getConverter() == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter for class [" + mapping.getClass() + "]" +
                            " for mapping [" + mapping.getName() + "]");
                }
            }
        }
    }

    private void clearRootClassMappingState() {
        chainedComponents.clear();
    }

}
