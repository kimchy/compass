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

package org.compass.core.mapping;

/**
 * An alias mapping is a mapping that is associated with an alias.
 *
 * @author kimchy
 */
public interface AliasMapping extends MultipleMapping {

    /**
     * Performs a shalow copy of this mapping, not including any internal mappings
     * belonging to {@link MultipleMapping}.
     */
    AliasMapping shallowCopy();

    /**
     * Returns the alias this mapping is associated with.
     */
    String getAlias();

    /**
     * Returns a list of aliases that this alias extends.
     */
    String[] getExtendedAliases();

    /**
     * Returns a list of all the aliases that extend this mapping. Note,
     * this is a list of all the aliases down the food chain, not just the
     * first ones.
     */
    String[] getExtendingAliases();

    /**
     * Returns the analyzer lookup name of this alias mapping.
     */
    String getAnalyzer();
}
