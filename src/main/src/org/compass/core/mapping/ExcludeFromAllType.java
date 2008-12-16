/*
 * Copyright 2004-2008 the original author or authors.
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

import org.compass.core.util.Parameter;

/**
 * Specifies if a certain property should be excluded from all property or not.
 *
 * @author kimchy
 */
public class ExcludeFromAllType extends Parameter {

    private ExcludeFromAllType(String name) {
        super(name);
    }

    /**
     * The property will not be excluded from all. If this property is "not_analyzed"
     * it will be added as is to the all property without being analyzed/tokenized.
     */
    public static final ExcludeFromAllType NO = new ExcludeFromAllType("NO");

    /**
     * The property will be exlcuded from all.
     */
    public static final ExcludeFromAllType YES = new ExcludeFromAllType("YES");

    /**
     * The property will not be excluded from all. If this property is "not_analyzed"
     * it will be added as is to the all property after being analyzed.
     */
    public static final ExcludeFromAllType NO_ANALYZED = new ExcludeFromAllType("NO_ANALYZED");

    public static ExcludeFromAllType fromString(String excludeFromAllType) {
        if ("no".equalsIgnoreCase(excludeFromAllType) || "false".equalsIgnoreCase(excludeFromAllType)) {
            return ExcludeFromAllType.NO;
        } else if ("yes".equalsIgnoreCase(excludeFromAllType) || "true".equalsIgnoreCase(excludeFromAllType)) {
            return ExcludeFromAllType.YES;
        } else if ("no_analyzed".equalsIgnoreCase(excludeFromAllType)) {
            return ExcludeFromAllType.NO_ANALYZED;
        }
        throw new IllegalArgumentException("Can't find exclude from all type for [" + excludeFromAllType + "]");
    }

    public static String toString(ExcludeFromAllType excludeFromAllType) {
        if (excludeFromAllType == NO) {
            return "no";
        }
        if (excludeFromAllType == NO_ANALYZED) {
            return "no_analyzed";
        }
        if (excludeFromAllType == YES) {
            return "yes";
        }
        return "no";
    }
}
