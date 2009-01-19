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
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;

/**
 * Uses a hash function based on hash code computation of alias and ids,
 * and using the modulo operation against the configured size.
 * <p/>
 * The sub index name is <code>prefix_(hashCode % size)</code>.
 * <p/>
 * If using configuration, expects <code>prefix</code> as the setting name,
 * and <code>size</code> as the modulo right hand side operation.
 *
 * @author kimchy
 */
public class ModuloSubIndexHash implements SubIndexHash, CompassConfigurable {

    private String prefix;

    private int size;

    /**
     * Constructs a new instance, will have to be configured.
     */
    public ModuloSubIndexHash() {
    }

    /**
     * Constructs a new instance, using the given prefix and size.
     *
     * @param prefix The prefix sed for the sub index prefix name
     * @param size   The size the modulo will be used
     */
    public ModuloSubIndexHash(String prefix, int size) {
        this.prefix = prefix;
        this.size = size;
    }

    /**
     * Using configuration, expects <code>prefix</code> as the setting name,
     * and <code>size</code> as the modulo right hand side operation.
     *
     * @param settings The setting to configure by
     * @throws CompassException
     */
    public void configure(CompassSettings settings) throws CompassException {
        prefix = settings.getSetting("prefix", null);
        if (prefix == null) {
            throw new ConfigurationException("prefix must be set for Modulo sub index hash");
        }
        size = settings.getSettingAsInt("size", -1);
        if (size < 0) {
            throw new ConfigurationException("size must be set for Modulo sub index hash");
        }
    }

    /**
     * Returns all the sub indexes possible, basically in the form of
     * <code>prefix_(0..size)</code> (not including size).
     */
    public String[] getSubIndexes() {
        String[] subIndexes = new String[size];
        for (int i = 0; i < size; i++) {
            subIndexes[i] = prefix + '_' + i;
        }
        return subIndexes;
    }

    /**
     * Computes the hash code of alias and all the ids, and then modulo it by size.
     * The prefix is prepended (with an underscore), and the sub index is constructed.
     * The formula is: <code>prefix_(hashCode % size)</code>.
     */
    public String mapSubIndex(String alias, Property[] ids) throws SearchEngineException {
        int hash = alias.hashCode();
        for (int i = 0; i < ids.length; i++) {
            hash = hash * 31 + ids[i].getStringValue().hashCode();
        }
        return prefix + '_' + (Math.abs(hash) % size);
    }


    public String toString() {
        return "ModuloSubIndexHash[prefix[" + prefix + "] size[" + size + "]]";
    }
}
