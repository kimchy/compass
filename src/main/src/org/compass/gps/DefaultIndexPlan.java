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

package org.compass.gps;

import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class DefaultIndexPlan implements IndexPlan {

    private Class[] types;

    private String[] aliases;

    private String[] subIndexes;

    public Class[] getTypes() {
        return types;
    }

    public IndexPlan setTypes(Class ... types) {
        this.types = types;
        return this;
    }

    public String[] getAliases() {
        return aliases;
    }

    public IndexPlan setAliases(String ... aliases) {
        this.aliases = aliases;
        return this;
    }

    public String[] getSubIndexes() {
        return subIndexes;
    }

    public IndexPlan setSubIndexes(String ... subIndexes) {
        this.subIndexes = subIndexes;
        return this;
    }

    public String toString() {
        return "subIndexes[" + StringUtils.arrayToCommaDelimitedString(getSubIndexes()) + "], aliases[" +
                StringUtils.arrayToCommaDelimitedString(getAliases()) + "], types [" +
                StringUtils.arrayToCommaDelimitedString(getTypes()) + "]";

    }
}
