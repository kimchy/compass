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

package org.compass.core.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.compass.core.CompassException;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.AliasedObject;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class CascadingManager {

    private InternalCompassSession session;

    private CompassMapping mapping;

    public CascadingManager(InternalCompassSession session) {
        this.session = session;
        this.mapping = session.getMapping();
    }

    public boolean cascade(Object root, CascadeMapping.Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        if (root instanceof AliasedObject) {
            return cascade(((AliasedObject) root).getAlias(), root, cascade);
        }
        return cascade(root.getClass(), root, cascade);
    }

    public boolean cascade(String alias, Object root, CascadeMapping.Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByAlias(alias);
        if (resourceMapping == null) {
            return false;
        }
        return cascade(resourceMapping, root, cascade);
    }

    public boolean cascade(Class clazz, Object root, CascadeMapping.Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        ResourceMapping resourceMapping = mapping.getMappingByClass(clazz);
        if (resourceMapping == null) {
            return false;
        }
        return cascade(resourceMapping, root, cascade);
    }

    private boolean cascade(ResourceMapping resourceMapping, Object root, CascadeMapping.Cascade cascade) throws CompassException {
        if (cascadingDisabled()) return false;
        CascadeMapping[] cascadeMappings = resourceMapping.getCascadeMappings();
        if (cascadeMappings == null) {
            return false;
        }
        boolean retVal = false;
        for (int i = 0; i < cascadeMappings.length; i++) {
            CascadeMapping cascadeMapping = cascadeMappings[i];
            if (cascadeMapping.shouldCascade(cascade)) {
                retVal = true;
                Object value = cascadeMapping.getCascadeValue(root);
                if (value == null) {
                    continue;
                }
                if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int j = 0; j < length; j++) {
                        cascadeOperation(cascade, Array.get(value, j));
                    }
                } else if (value instanceof Collection) {
                    for (Iterator it = ((Collection) value).iterator(); it.hasNext();) {
                        cascadeOperation(cascade, it.next());
                    }
                } else {
                    cascadeOperation(cascade, value);
                }

            }
        }
        return retVal;
    }

    private void cascadeOperation(CascadeMapping.Cascade cascade, Object value) {
        // TODO what happens if there are several aliases for value
        if (value == null) {
            return;
        }
        if (cascade == CascadeMapping.Cascade.DELETE) {
            session.delete(value);
        } else if (cascade == CascadeMapping.Cascade.CREATE) {
            session.create(value);
        } else if (cascade == CascadeMapping.Cascade.SAVE) {
            session.save(value);
        } else {
            throw new IllegalArgumentException("Failed to perform cascading unknown type [" + cascade + "]");
        }
    }

    private boolean cascadingDisabled() {
        return session.getSettings().getSettingAsBoolean(CompassEnvironment.Cascade.DISABLE, false);
    }

}
