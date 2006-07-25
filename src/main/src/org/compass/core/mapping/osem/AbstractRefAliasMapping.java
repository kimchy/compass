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

import java.util.HashMap;


/**
 * @author kimchy
 */
public abstract class AbstractRefAliasMapping extends AbstractAccessorMapping implements HasRefAliasMapping {

    private String[] refAliases;

    private ClassMapping[] refClassMappings;

    private Class refClass;
    
    private HashMap refAliasesMap = new HashMap();

    protected void copy(AbstractRefAliasMapping mapping) {
        super.copy(mapping);
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
    
    public boolean hasAtLeastOnRefAlias(String[] refAliases) {
        if (refAliases == null) {
            return false;
        }
        for (int i = 0; i < refAliases.length; i++) {
            if (hasRefAlias(refAliases[i])) {
                return true;
            }
        }
        return false;
    }
    
    public ClassMapping getRefClassMapping(String alias) {
        return (ClassMapping) refAliasesMap.get(alias);
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
            for (int i = 0; i < refClassMappings.length; i++) {
                refAliasesMap.put(refClassMappings[i].getAlias(), refClassMappings[i]);
            }
        }
    }

    public Class getRefClass() {
        return refClass;
    }

    public void setRefClass(Class refClass) {
        this.refClass = refClass;
    }
}
