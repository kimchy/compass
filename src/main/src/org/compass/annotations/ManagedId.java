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

package org.compass.annotations;

/**
 * For class proeprties ({@link SearchableProperty}, and {@link SearchableId}, Compass
 * might require an internal meta-data to be created, so it can identify the correct
 * value that match the property and preform proper unmarshalling.
 * Compass can create this internal meta-data automatcially by analyzing all the properties
 * in the class using the {@link #AUTO} option. It can also not create the internal
 * meta-data using {@link #FALSE} and use the first meta-data as the intenral id,
 * or always create the intenal meta-data using {@link #TRUE}.
 * The other options allow to not create an interanl id and never unmarshalling that
 * property ({@link #NO}), and not creating an internal id in case there all the meta
 * data mappings fro that property have store="no" ({@link #NO_STORE}). 
 *
 * @author kimchy
 */
public enum ManagedId {

    /**
     * Not set, will let Compass defaults (on the Searchable mapping and on the global
     * settings) to control this value.
     */
    NA,

    /**
     * Compass will analyzer all the class mappings, and only create an
     * internal id if one is required.
     */
    AUTO,

    /**
     * Compass will always create an intenral meta-data for the property.
     */
    TRUE,
    
    /**
     * Compass will never create an internal meta-data for the property.
     */
    FALSE,

    /**
     * Compess will not create an internal id for this proeprty. It will also
     * not try and unmarshall this property from the index.
     */
    NO,

    /**
     * Compass will not create an internal id for this property if all of its
     * meta data created have store="no". In this case, it will also not try and
     * unmarshall it from the index.
     */
    NO_STORE
}
