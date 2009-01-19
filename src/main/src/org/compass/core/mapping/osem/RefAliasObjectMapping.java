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

/**
 * A mapping that references other mappings.
 *
 * @author kimchy
 */
public interface RefAliasObjectMapping extends ObjectMapping {

    boolean hasRefAlias(String refAlias);

    boolean hasAtLeastOneRefAlias(String[] refAliases);

    ClassMapping getRefClassMapping(String alias);

    String[] getRefAliases();

    ClassMapping[] getRefClassMappings();

    /**
     * Returns the reference class which will be used if no ref aliases are explicitly
     * defined using {@link #getRefAliases()} to identify the alias.
     */
    Class getRefClass();
}
