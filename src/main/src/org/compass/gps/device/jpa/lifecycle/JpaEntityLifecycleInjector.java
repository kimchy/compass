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

package org.compass.gps.device.jpa.lifecycle;

import javax.persistence.EntityManagerFactory;

import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * <p>A global lifecycle event listener injector. Since the <code>EntityManagerFactory</code> does not
 * allow for setting global lifecycle event listeners, actual implementations of the JPA spec can be
 * used directly to inject global lifecycle event listeners usign propriety APIs.
 *
 * <p>Assume that the <code>EntityManagerFactory</code> is the native one, since the
 * {@link org.compass.gps.device.jpa.extractor.NativeJpaExtractor} of the
 * {@link JpaGpsDevice} was used to extract it.
 *
 * @author kimchy
 * @see org.compass.gps.device.jpa.lifecycle.HibernateJpaEntityLifecycleInjector
 * @see JpaEntityLifecycleInjectorDetector
 */
public interface JpaEntityLifecycleInjector {

    /**
     * Injects a global lifecycle listener into the concrete <code>EntityManagerFactory<code>
     * implementation.
     *
     * @param entityManagerFactory The <code>EntityManagerFactory</code> to inject the global lifecycle to.
     * @param device               The Jpa device calling this injector
     * @throws JpaGpsDeviceException
     */
    void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException;

    /**
     * Removes (if possible) lifecycle listeners injected using the inject method.
     *
     * @param entityManagerFactory The EMF to remove lifecycle from
     * @param device               The Jpa device calling
     * @throws JpaGpsDeviceException
     */
    void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException;

    /**
     * Does this injector requires refreshing (i.e. remove and inject) of new listeners upon
     * {@link org.compass.gps.CompassGpsDevice#refresh()}.
     */
    boolean requireRefresh();
}
