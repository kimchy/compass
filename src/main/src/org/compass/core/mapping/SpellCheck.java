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
 * Specifies if a certain property should be included in the spell check index.
 *
 * <p>Note, most times this is not requried to be configured, since by default, the
 * spell check index uses the "all" property.
 * 
 * @author kimchy
 */
public enum SpellCheck {

    /**
     * Should this property mapping be included in the spell check index.
     */
    INCLUDE,

    /**
     * Should this proeprty mapping be excluded from the spell check index.
     */
    EXCLUDE,

    /**
     * NA. Has no affect on the spell check and will use external properties configuration.
     */
    NA;

    /**
     * Converts to spell check type from a String.
     */
    public static SpellCheck fromString(String spellCheckType) {
        if ("include".equalsIgnoreCase(spellCheckType)) {
            return SpellCheck.INCLUDE;
        } else if ("exclude".equalsIgnoreCase(spellCheckType)) {
            return SpellCheck.EXCLUDE;
        } else if ("na".equalsIgnoreCase(spellCheckType)) {
            return SpellCheck.NA;
        }
        throw new IllegalArgumentException("Can't find spell check type for [" + spellCheckType + "]");
    }

    /**
     * Converts the spell check type to a String.
     */
    public static String toString(SpellCheck spellCheckType) {
        if (spellCheckType == INCLUDE) {
            return "include";
        }
        if (spellCheckType == EXCLUDE) {
            return "exlcude";
        }
        if (spellCheckType == NA) {
            return "na";
        }
        return "na";
    }
}
