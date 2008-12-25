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

package org.compass.core.mapping.osem;

import org.compass.core.util.Parameter;

/**
 * Controls how Compass managed ids will be created.
 *
 * @author kimchy
 */
public final class ManagedId extends Parameter {

    private static final long serialVersionUID = -7849904473959816389L;

    ManagedId(String name) {
        super(name);
    }

    /**
     * The meta-data (resource-property) that will act as the id will be
     * computed automatically.
     */
    public static final ManagedId AUTO = new ManagedId("AUTO");

    /**
     * The class property will always have an internal managed id that will
     * be created.
     */
    public static final ManagedId TRUE = new ManagedId("TRUE");

    /**
     * The class property will not have an internal managed id, the
     * meta-data that will be used as an id will be the first one.
     */
    public static final ManagedId FALSE = new ManagedId("FALSE");

    /**
     * The class proeprty will not create an internal managed id if
     * all its meta data mappings have store=no
     */
    public static final ManagedId NO_STORE = new ManagedId("NO_STORE");

    /**
     * The class property will not have any internal meta-data id, causing
     * it not to be unmarshalled at all.
     */
    public static final ManagedId NO = new ManagedId("NO");

    public static String toString(ManagedId managedId) {
        if (managedId == ManagedId.AUTO) {
            return "auto";
        } else if (managedId == ManagedId.TRUE) {
            return "true";
        } else if (managedId == ManagedId.FALSE) {
            return "false";
        } else if (managedId == ManagedId.NO_STORE) {
            return "no_store";
        } else if (managedId == ManagedId.NO) {
            return "no";
        }
        throw new IllegalArgumentException("Can't find managed-id for [" + managedId + "]");
    }

    public static ManagedId fromString(String managedId) {
        if ("auto".equalsIgnoreCase(managedId)) {
            return ManagedId.AUTO;
        } else if ("true".equalsIgnoreCase(managedId)) {
            return ManagedId.TRUE;
        } else if ("false".equalsIgnoreCase(managedId)) {
            return ManagedId.FALSE;
        } else if ("no_store".equalsIgnoreCase(managedId)) {
            return ManagedId.NO_STORE;
        } else if ("no".equalsIgnoreCase(managedId)) {
            return ManagedId.NO;
        }
        throw new IllegalArgumentException("Can't find managed-id for [" + managedId + "]");
    }

}
