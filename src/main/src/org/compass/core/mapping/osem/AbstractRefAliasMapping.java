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

import java.util.HashMap;

import org.compass.core.CompassException;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.internal.InternalCascadeMapping;
import org.compass.core.mapping.osem.internal.InternalRefAliasObjectMapping;


/**
 * @author kimchy
 */
public abstract class AbstractRefAliasMapping extends AbstractAccessorMapping implements InternalRefAliasObjectMapping, InternalCascadeMapping {

    private String[] refAliases;

    private ClassMapping[] refClassMappings;

    private Class refClass;

    private HashMap<String, ClassMapping> refAliasesMap = new HashMap<String, ClassMapping>();

    private Cascade[] cascades;

    private Boolean shouldCascadeDelete;
    private Boolean shouldCascadeCreate;
    private Boolean shouldCascadeSave;

    protected void copy(AbstractRefAliasMapping mapping) {
        super.copy(mapping);
        mapping.setCascades(cascades);
        if (refAliases != null) {
            String[] copyRefAliases = new String[refAliases.length];
            System.arraycopy(refAliases, 0, copyRefAliases, 0, refAliases.length);
            mapping.setRefAliases(copyRefAliases);
        }
        mapping.setRefClass(getRefClass());
        if (refClassMappings != null) {
            ClassMapping[] copyRefClassMappings = new ClassMapping[refClassMappings.length];
            System.arraycopy(refClassMappings, 0, copyRefClassMappings, 0, refClassMappings.length);
            mapping.setRefClassMappings(copyRefClassMappings);
        }
    }

    public boolean hasRefAlias(String refAlias) {
        return refAliasesMap.get(refAlias) != null;
    }

    public boolean hasAtLeastOneRefAlias(String[] refAliases) {
        if (refAliases == null) {
            return false;
        }
        for (String refAlias : refAliases) {
            if (hasRefAlias(refAlias)) {
                return true;
            }
        }
        return false;
    }

    public ClassMapping getRefClassMapping(String alias) {
        return refAliasesMap.get(alias);
    }

    public String[] getRefAliases() {
        return refAliases;
    }

    public void setRefAliases(String[] refAliases) {
        this.refAliases = refAliases;
    }

    public ClassMapping[] getRefClassMappings() {
        return refClassMappings;
    }

    public void setRefClassMappings(ClassMapping[] refClassMappings) {
        this.refClassMappings = refClassMappings;
        if (refClassMappings != null) {
            for (ClassMapping refClassMapping : refClassMappings) {
                refAliasesMap.put(refClassMapping.getAlias(), refClassMapping);
            }
        }
    }

    public Class getRefClass() {
        return refClass;
    }

    public void setRefClass(Class refClass) {
        this.refClass = refClass;
    }

    public Cascade[] getCascades() {
        return cascades;
    }

    public void setCascades(Cascade[] cascades) {
        this.cascades = cascades;
    }

    public Object getCascadeValue(Object root) throws CompassException {
        return getGetter().get(root);
    }

    public boolean shouldCascadeDelete() {
        if (cascades == null) {
            return false;
        }
        if (shouldCascadeDelete != null) {
            return shouldCascadeDelete;
        }
        for (Cascade cascade : cascades) {
            if (cascade == Cascade.DELETE || cascade == Cascade.ALL) {
                shouldCascadeDelete = Boolean.TRUE;
            }
        }
        if (shouldCascadeDelete == null) {
            shouldCascadeDelete = Boolean.FALSE;
        }
        return shouldCascadeDelete;
    }


    public boolean shouldCascadeCreate() {
        if (cascades == null) {
            return false;
        }
        if (shouldCascadeCreate != null) {
            return shouldCascadeCreate;
        }
        for (Cascade cascade : cascades) {
            if (cascade == Cascade.CREATE || cascade == Cascade.ALL) {
                shouldCascadeCreate = Boolean.TRUE;
            }
        }
        if (shouldCascadeCreate == null) {
            shouldCascadeCreate = Boolean.FALSE;
        }
        return shouldCascadeCreate;
    }

    public boolean shouldCascadeSave() {
        if (cascades == null) {
            return false;
        }
        if (shouldCascadeSave != null) {
            return shouldCascadeSave;
        }
        for (Cascade cascade : cascades) {
            if (cascade == Cascade.SAVE || cascade == Cascade.ALL) {
                shouldCascadeSave = Boolean.TRUE;
            }
        }
        if (shouldCascadeSave == null) {
            shouldCascadeSave = Boolean.FALSE;
        }
        return shouldCascadeSave;
    }


    public boolean shouldCascade(Cascade cascade) {
        if (cascades == null || cascades.length == 0) {
            return false;
        }
        if (cascade == Cascade.ALL) {
            // if we pass ALL, it means that any cascading is enough (since cascades is not null, there is at least one)
            return true;
        }else if (cascade == Cascade.CREATE) {
            return shouldCascadeCreate();
        } else if (cascade == Cascade.SAVE) {
            return shouldCascadeSave();
        } else if (cascade == Cascade.DELETE) {
            return shouldCascadeDelete();
        } else {
            throw new IllegalArgumentException("Should cascade can't handle [" + cascade + "]");
        }
    }
}
