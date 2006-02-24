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

package org.compass.annotations;

/**
 * For class proeprties ({@link SearchableProperty}, and {@link SearchableId}, Compass
 * might require an internal meta-data to be created, so it can identify the correct
 * value that match the property. Compass can create this internal meta-data automatcially by
 * analyzing all the properties in the class using the {@link #AUTO} option. It can also
 * not create the internal meta-data using {@link #FALSE}, or always create the intenal
 * meta-data using {@link #TRUE}.
 *
 * @author kimchy
 */
public enum ManagedId {
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
    FALSE
}
