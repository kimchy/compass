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

package org.compass.core.cascade;

import java.lang.reflect.Array;
import java.util.Collection;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.AliasedObject;
import org.compass.core.spi.DirtyOperationContext;
import org.compass.core.spi.InternalCompassSession;

/**
 * Cascading manager supports perfoming cascade opeations on Objects.
 *
 * @author kimchy
 */
public class CascadingManager implements CompassConfigurable {

    private InternalCompassSession session;

    private CompassMapping mapping;

    private CompassCascadeFilter cascadeFilter;

    public CascadingManager(InternalCompassSession session) {
        this.session = session;
        this.mapping = session.getMapping();
        configure(session.getCompass().getSettings());
    }

    public void configure(CompassSettings settings) throws CompassException {
        String filterName = settings.getSetting(CompassEnvironment.Cascade.FILTER_TYPE);
        if (filterName != null) {
            try {
                Class filterClass = settings.getSettingAsClass(CompassEnvironment.Cascade.FILTER_TYPE, CompassCascadeFilter.class);
                cascadeFilter = (CompassCascadeFilter) filterClass.newInstance();
            }
            catch (Exception e) {
                throw new ConfigurationException("Unable to create cascade filter of class " + filterName, e);
            }
        }
    }

    public boolean cascade(Object root, Cascade cascade, DirtyOperationContext context) throws CompassException {
        if (cascadingDisabled()) return false;
        if (root instanceof AliasedObject) {
            return cascade(((AliasedObject) root).getAlias(), root, cascade, context);
        }
        return cascade(root.getClass(), root, cascade, context);
    }

    public boolean cascade(String alias, Object root, Cascade cascade, DirtyOperationContext context) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByAlias(alias);
        return resourceMapping != null && cascade(resourceMapping, root, cascade, context);
    }

    public boolean cascade(Class clazz, Object root, Cascade cascade, DirtyOperationContext context) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByClass(clazz);
        return resourceMapping != null && cascade(resourceMapping, root, cascade, context);
    }

    public boolean shouldCascade(Object root, Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        if (root instanceof AliasedObject) {
            return shouldCascade(((AliasedObject) root).getAlias(), root, cascade);
        }
        return shouldCascade(root.getClass(), root, cascade);
    }

    public boolean shouldCascade(String alias, Object root, Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByAlias(alias);
        return resourceMapping != null && shouldCascade(resourceMapping, root, cascade);
    }

    public boolean shouldCascade(Class clazz, Object root, Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByClass(clazz);
        return resourceMapping != null && shouldCascade(resourceMapping, root, cascade);
    }

    private boolean shouldCascade(ResourceMapping resourceMapping, Object root, Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        CascadeMapping[] cascadeMappings = resourceMapping.getCascadeMappings();
        if (cascadeMappings == null) {
            return false;
        }
        for (CascadeMapping cascadeMapping : cascadeMappings) {
            if (cascadeMapping.shouldCascade(cascade)) {
                return true;
            }
        }
        return false;
    }


    private boolean cascade(ResourceMapping resourceMapping, Object root, Cascade cascade, DirtyOperationContext context) throws CompassException {
        if (cascadingDisabled()) return false;
        CascadeMapping[] cascadeMappings = resourceMapping.getCascadeMappings();
        if (cascadeMappings == null) {
            return false;
        }
        boolean retVal = false;
        for (CascadeMapping cascadeMapping : cascadeMappings) {
            if (cascadeMapping.shouldCascade(cascade)) {
                retVal = true;
                Object value = cascadeMapping.getCascadeValue(root);
                if (value == null) {
                    continue;
                }
                if (value instanceof Object[]) {
                    int length = Array.getLength(value);
                    for (int j = 0; j < length; j++) {
                        cascadeOperation(cascade, Array.get(value, j), context);
                    }
                } else if (value instanceof Collection) {
                    for (Object o : ((Collection) value)) {
                        cascadeOperation(cascade, o, context);
                    }
                } else {
                    cascadeOperation(cascade, value, context);
                }

            }
        }
        return retVal;
    }

    private void cascadeOperation(Cascade cascade, Object value, DirtyOperationContext context) {
        // TODO what happens if there are several aliases for value
        if (value == null) {
            return;
        }
        if (cascade == Cascade.DELETE) {
            if (cascadeFilter != null && cascadeFilter.shouldFilterDelete(value)) {
                return;
            }
            session.delete(value, context);
        } else if (cascade == Cascade.CREATE) {
            if (cascadeFilter != null && cascadeFilter.shouldFilterCreate(value)) {
                return;
            }
            session.create(value, context);
        } else if (cascade == Cascade.SAVE) {
            if (cascadeFilter != null && cascadeFilter.shouldFilterSave(value)) {
                return;
            }
            session.save(value, context);
        } else {
            throw new IllegalArgumentException("Failed to perform cascading unknown type [" + cascade + "]");
        }
    }

    private boolean cascadingDisabled() {
        return session.getSettings().getSettingAsBoolean(CompassEnvironment.Cascade.DISABLE, false);
    }
}
