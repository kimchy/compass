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

package org.compass.core.mapping;

/**
 * @author kimchy
 */
public class ContractMapping extends AbstractMultipleMapping implements AliasMapping {

    private String alias;

    private String[] extendedAliases;

    private String[] extendingAliases;

    public Mapping copy() {
        AliasMapping contractMapping = shallowCopy();
        super.copy(contractMapping);
        return contractMapping;
    }

    public AliasMapping shallowCopy() {
        ContractMapping contractMapping = new ContractMapping();
        contractMapping.setAlias(getAlias());
        contractMapping.setExtendedAliases(getExtendedAliases());
        contractMapping.setExtendingAliases(getExtendingAliases());
        return contractMapping;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String[] getExtendedAliases() {
        return extendedAliases;
    }

    public void setExtendedAliases(String[] extendedMappings) {
        this.extendedAliases = extendedMappings;
    }

    public String[] getExtendingAliases() {
        return extendingAliases;
    }

    public void setExtendingAliases(String[] extendingAliases) {
        this.extendingAliases = extendingAliases;
    }
}
