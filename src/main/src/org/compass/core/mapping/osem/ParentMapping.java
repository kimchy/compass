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

import org.compass.core.CompassException;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.Mapping;


/**
 * @author kimchy
 */
public class ParentMapping extends AbstractAccessorMapping implements CascadeMapping {

    private Cascade[] cascades;

    private Boolean shouldCascadeDelete;
    private Boolean shouldCascadeCreate;
    private Boolean shouldCascadeSave;

    public Mapping copy() {
        ParentMapping copy = new ParentMapping();
        super.copy(copy);
        copy.setName(getName());
        copy.setSetter(getSetter());
        copy.setGetter(getGetter());
        copy.setCascades(cascades);
        return copy;
    }

    public boolean canBeCollectionWrapped() {
        return false;
    }

    public boolean controlsObjectNullability() {
        return false;
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
            return shouldCascadeDelete.booleanValue();
        }
        for (int i = 0; i < cascades.length; i++) {
            if (cascades[i] == Cascade.DELETE || cascades[i] == Cascade.ALL) {
                shouldCascadeDelete = Boolean.TRUE;
            }
        }
        if (shouldCascadeDelete == null) {
            shouldCascadeDelete = Boolean.FALSE;
        }
        return shouldCascadeDelete.booleanValue();
    }


    public boolean shouldCascadeCreate() {
        if (cascades == null) {
            return false;
        }
        if (shouldCascadeCreate != null) {
            return shouldCascadeCreate.booleanValue();
        }
        for (int i = 0; i < cascades.length; i++) {
            if (cascades[i] == Cascade.CREATE || cascades[i] == Cascade.ALL) {
                shouldCascadeCreate = Boolean.TRUE;
            }
        }
        if (shouldCascadeCreate == null) {
            shouldCascadeCreate = Boolean.FALSE;
        }
        return shouldCascadeCreate.booleanValue();
    }

    public boolean shouldCascadeSave() {
        if (cascades == null) {
            return false;
        }
        if (shouldCascadeSave != null) {
            return shouldCascadeSave.booleanValue();
        }
        for (int i = 0; i < cascades.length; i++) {
            if (cascades[i] == Cascade.SAVE || cascades[i] == Cascade.ALL) {
                shouldCascadeSave = Boolean.TRUE;
            }
        }
        if (shouldCascadeSave == null) {
            shouldCascadeSave = Boolean.FALSE;
        }
        return shouldCascadeSave.booleanValue();
    }


    public boolean shouldCascade(Cascade cascade) {
        if (cascades == null || cascades.length == 0) {
            return false;
        }
        if (cascade == Cascade.ALL) {
            // if we pass ALL, it means that any cascading is enough (since cascades is not null, there is at least one)
            return true;
        } else if (cascade == Cascade.CREATE) {
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
