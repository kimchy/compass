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

package org.compass.gps.device.jpa.extractor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * A native <code>EntityManagerFactory<code> extractor. Should be used with code/frameworks that
 * wrap the actual <code>EntityManagerFactory</code>.
 *
 * @author kimchy
 */
public interface NativeJpaExtractor {

    /**
     * Returns the actual <code>EntityManagerFactory</code> based on the given <code>EntityManagerFactory</code>.
     * Should return the same <code>EntityManagerFactory</code> if not wrapping has been done.
     *
     * @param entityManagerFactory The (possibly) wrapped <code>EntityManagerFactory</code>.
     * @return The actual <code>EntityManagerFactory</code> implementation.
     * @throws org.compass.gps.device.jpa.JpaGpsDeviceException
     */
    EntityManagerFactory extractNative(EntityManagerFactory entityManagerFactory) throws JpaGpsDeviceException;

    /**
     * Extracts the native <code>EntityManager</code> based on the given <code>EntityManager</code>.
     * Should return the same <code>EntityManager</code> if no wrapper has been done.
     *
     * @param entityManager The (possibly) wrapper <code>EntityManager</code>.
     * @return The actual <code>EntityManager</code>.
     * @throws JpaGpsDeviceException
     */
    EntityManager extractNative(EntityManager entityManager) throws JpaGpsDeviceException;
}
