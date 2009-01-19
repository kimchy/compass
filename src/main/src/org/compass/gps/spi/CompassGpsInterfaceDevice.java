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

package org.compass.gps.spi;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ResourceMapping;
import org.compass.gps.CompassGps;

/**
 * An extension to the {@link org.compass.gps.CompassGps} interface
 * which should be used by devices. Provides abstraction from the actual
 * <code>Compass</code> instances management.
 * 
 * @author kimchy
 */
public interface CompassGpsInterfaceDevice extends CompassGps {

    /**
     * Returns <code>true</code> if there is mapping for the given class when
     * performing the mirror operation.
     */
    boolean hasMappingForEntityForMirror(Class clazz, Cascade cascade) throws CompassException;

    /**
     * Returns <code>true</code> if there is mapping for the given name (alias
     * or class name) when performing the mirror operation.
     */
    boolean hasMappingForEntityForMirror(String name, Cascade cascade) throws CompassException;

    /**
     * Returns <code>true</code> if there is mapping for the given class when
     * performing the index operation.
     */
    boolean hasMappingForEntityForIndex(Class clazz) throws CompassException;

    /**
     * Returns <code>true</code> if there is mapping for the given name (alias
     * or class name) when performing the index operation.
     */
    boolean hasMappingForEntityForIndex(String name) throws CompassException;

    /**
     * Returns the mapping of the given name (alias or class name) when performing
     * the index operation.
     */
    ResourceMapping getMappingForEntityForIndex(String name) throws CompassException;

    /**
     * Returns the mapping for the given class name when performing the index operation.
     */
    ResourceMapping getMappingForEntityForIndex(Class clazz) throws CompassException;

    /**
     * Executes the given callback for index operations.
     */
    void executeForIndex(CompassCallback callback) throws CompassException;

    /**
     * Executes the given callback for mirror operations.
     */
    void executeForMirror(CompassCallback callback) throws CompassException;

    /**
     * Returns the <code>Compass</code> instance used for indexing. Note that
     * no operations that will affect the index should be made using it, use
     * {@link #executeForIndex(CompassCallback)} operation instead.
     */
    Compass getIndexCompass();

    /**
     * Returns the <code>Compass</code> instance used for mirroring. Note that
     * no operations that will affect the index should be made using it, use
     * {@link #executeForMirror(CompassCallback)} operation instead.
     */
    Compass getMirrorCompass();
}
