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

import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.InternalAllMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.internal.InternalResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.CollectionMapping;
import org.compass.core.mapping.osem.LazyMapping;
import org.compass.core.mapping.osem.internal.InternalLazyMapping;

/**
 * Reolves late attributes associated usually with {@link ClassMapping}, they are:
 *
 * <p>SupportUnmarshall: One can set globally if ClassMappings will support unmarshalling or not.
 *
 * @author kimchy
 */
public class ResolveLateAttributesPreLateBindingMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        ((InternalCompassMapping) compassMapping).setPath(namingStrategy.getRootPath());
        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) aliasMapping;
                if (!classMapping.isSupportUnmarshallSet()) {
                    classMapping.setSupportUnmarshall(settings.getSettingAsBoolean(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, true));
                }
                if (classMapping.isFilterDuplicates() == null) {
                    classMapping.setFilterDuplicates(settings.getSettingAsBoolean(CompassEnvironment.Osem.FILTER_DUPLICATES, false));
                }
                processClassMappingProperties(classMapping, settings);
            }
            if (aliasMapping instanceof ResourceMapping) {
                ResourceMapping resourceMapping = (ResourceMapping) aliasMapping;
                if (resourceMapping.getAllMapping().isSupported() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setSupported(settings.getSettingAsBoolean(CompassEnvironment.All.ENABLED, true));
                }
                if (resourceMapping.getAllMapping().isExcludeAlias() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setExcludeAlias(settings.getSettingAsBoolean(CompassEnvironment.All.EXCLUDE_ALIAS, true));
                }
                if (resourceMapping.getAllMapping().isOmitNorms() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setOmitNorms(settings.getSettingAsBoolean(CompassEnvironment.All.OMIT_NORMS, false));
                }
                if (resourceMapping.getAllMapping().isOmitTf() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setOmitTf(settings.getSettingAsBoolean(CompassEnvironment.All.OMIT_TF, false));
                }
                if (resourceMapping.getAllMapping().getProperty() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setProperty(settings.getSetting(CompassEnvironment.All.NAME, CompassEnvironment.All.DEFAULT_NAME));
                }
                if (resourceMapping.getAllMapping().getTermVector() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setTermVector(Property.TermVector.fromString(
                            settings.getSetting(CompassEnvironment.All.TERM_VECTOR, Property.TermVector.NO.toString())));
                }
                if (resourceMapping.getAllMapping().isIncludePropertiesWithNoMappings() == null) {
                    ((InternalAllMapping) resourceMapping.getAllMapping()).setIncludePropertiesWithNoMappings(settings.getSettingAsBoolean(CompassEnvironment.All.INCLUDE_UNMAPPED_PROPERTIES, true));
                }
            }
            if (aliasMapping instanceof InternalResourceMapping) {
                InternalResourceMapping resourceMapping = (InternalResourceMapping) aliasMapping;
                SpellCheck globablSpellCheck = SpellCheck.fromString(settings.getSetting(LuceneEnvironment.SpellCheck.DEFAULT_MODE, "NA"));
                if (resourceMapping.getSpellCheck() == SpellCheck.NA) {
                    resourceMapping.setSpellCheck(globablSpellCheck);
                }
            }
        }

        return compassMapping;
    }

    private void processClassMappingProperties(ClassMapping classMapping, CompassSettings settings) {
        boolean defaultLazy = settings.getSettingAsBoolean(CompassEnvironment.Osem.LAZY_REFERNCE, false);
        for (Iterator<Mapping> it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping m = it.next();
            if (m instanceof LazyMapping) {
                LazyMapping lazyMapping = (LazyMapping) m;
                // only collection mapping are lazy
                if (lazyMapping instanceof CollectionMapping) {
                    CollectionMapping collectionMapping = (CollectionMapping) lazyMapping;
                    if (collectionMapping.getElementMapping() instanceof LazyMapping) {
                        LazyMapping elementMapping = (LazyMapping) collectionMapping.getElementMapping();
                        if (elementMapping.isLazy() == null) {
                            ((InternalLazyMapping) elementMapping).setLazy(defaultLazy);
                            collectionMapping.setLazy(defaultLazy);
                        }
                    }
                } else {
                    ((InternalLazyMapping) lazyMapping).setLazy(false);
                }
            }
        }
    }
}
