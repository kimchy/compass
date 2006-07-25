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
import org.compass.core.converter.xsem.SimpleXmlValueConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.mapping.xsem.XmlIdMapping;
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
            } else if (m instanceof XmlObjectMapping) {
                secondPass((XmlObjectMapping) m, compassMapping);
            }
        }

        return compassMapping;
    }

    private void secondPass(XmlObjectMapping xmlObjectMapping, CompassMapping fatherMapping) {
        xmlObjectMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), xmlObjectMapping.getAlias()));
        secondPassConverter(xmlObjectMapping, true);
        for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            secondPassConverter(mapping, true);
            if (mapping instanceof XmlIdMapping) {
                XmlIdMapping xmlIdMapping = (XmlIdMapping) mapping;
                // in case of xml id mapping, we always use it as internal id
                // and build its own internal path (because other xml properties names might be dynamic)
                xmlIdMapping.setInternal(true);
                xmlIdMapping.setPath(namingStrategy.buildPath(xmlObjectMapping.getPath(), xmlIdMapping.getName()));
            }
            if (mapping instanceof XmlPropertyMapping) {
                XmlPropertyMapping xmlPropertyMapping = (XmlPropertyMapping) mapping;
                if (xmlPropertyMapping.getValueConverterName() != null) {
                    String converterName = xmlPropertyMapping.getValueConverterName();
                    xmlPropertyMapping.setValueConverter(converterLookup.lookupConverter(converterName));
                    if (xmlPropertyMapping.getValueConverter() == null) {
                        throw new ConfigurationException("Failed to find converter [" + converterName + "] for mapping " +
                                "[" + xmlPropertyMapping.getName() + "]");
                    }
                } else {
                    // this should probably be handled in the actual converteres
                    xmlPropertyMapping.setValueConverter(new SimpleXmlValueConverter());
                }
            }
        }
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
                MarshallingEnvironment.PROPERTY_CLASS).hintStatic());
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

        collectionMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), collectionMapping.getName()));
        collectionMapping.setCollectionTypePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_TYPE).hintStatic());
        collectionMapping.setColSizePath(namingStrategy.buildPath(collectionMapping.getPath(),
                MarshallingEnvironment.PROPERTY_COLLECTION_SIZE).hintStatic());

        return removeMapping;
    }

    private boolean secondPass(ReferenceMapping referenceMapping, Mapping fatherMapping) {
        referenceMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), referenceMapping.getName()));
        secondPassConverter(referenceMapping);
        ClassMapping[] refMappings = referenceMapping.getRefClassMappings();

        ClassMapping[] copyRefClassMappings = new ClassMapping[refMappings.length];
        for (int i = 0; i < refMappings.length; i++) {
            ClassMapping refClass = (ClassMapping) refMappings[i].copy();
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
            copyRefClassMappings[i] = refClass;
        }
        referenceMapping.setRefClassMappings(copyRefClassMappings);

        // now configure the component mapping if exists
        if (referenceMapping.getRefCompAlias() != null) {
            ClassMapping pointerClass = (ClassMapping) compassMapping.getResourceMappingByAlias(referenceMapping
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

    private boolean secondPass(ComponentMapping compMapping, Mapping fatherMapping) {
        int numberOfComponentsWithTheSameAlias = 0;
        for (Iterator it = chainedComponents.iterator(); it.hasNext();) {
            ComponentMapping tempComponentMapping = (ComponentMapping) it.next();
            if (compMapping.hasAtLeastOnRefAlias(tempComponentMapping.getRefAliases())) {
                numberOfComponentsWithTheSameAlias++;
            }
        }
        if (numberOfComponentsWithTheSameAlias >= compMapping.getMaxDepth()) {
            return true;
        }

        chainedComponents.add(compMapping);

        compMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), compMapping.getName()));
        secondPassConverter(compMapping);
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

    private boolean secondPass(ParentMapping parentMapping) {
        secondPassConverter(parentMapping);
        return false;
    }

    private boolean secondPass(ClassPropertyMapping classPropertyMapping, Mapping fatherMapping) {
        secondPassConverter(classPropertyMapping);
        classPropertyMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), classPropertyMapping.getName()));

        // we check if we override the managedId option
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
        constantMapping.setPath(new StaticPropertyPath(constantMapping.getName()));
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
