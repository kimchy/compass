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

package org.compass.core.engine.subindex;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;

/**
 * Alwats returns a constant sub index for any hashing.
 * <p/>
 * If using configuration, expects <code>subIndex</code> as the setting name,
 * and the sub index value as the setting value.
 *
 * @author kimchy
 */
public class ConstantSubIndexHash implements SubIndexHash, CompassConfigurable {

    private String subIndex;

    /**
     * Construts a new instances, will have to call {@link #configure(org.compass.core.config.CompassSettings)}
     * in order to configure the sub index.
     */
    public ConstantSubIndexHash() {
    }

    /**
     * Constructs a new instance based on the given sub index.
     *
     * @param subIndex The constant sub index to use
     */
    public ConstantSubIndexHash(String subIndex) {
        this.subIndex = subIndex.toLowerCase();
    }

    /**
     * Configures the constant sub index hash, expects <code>subIndex</code> as
     * the setting name, and the sub index value as the setting value.
     *
     * @param settings The settings to configure by
     * @throws CompassException
     */
    public void configure(CompassSettings settings) throws CompassException {
        subIndex = settings.getSetting("subIndex", subIndex).toLowerCase();
    }


    /**
     * Returns the single constant sub index.
     */
    public String[] getSubIndexes() {
        return new String[]{subIndex};
    }

    /**
     * Returns the single constant sub index. Does not take into account
     * either the alias, or the ids.
     */
    public String mapSubIndex(String alias, Property[] ids) throws SearchEngineException {
        return subIndex;
    }


    public String toString() {
        return "ConstantSubIndexHash[" + subIndex + "]";
    }
}
