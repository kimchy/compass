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

package org.compass.gps.device.hibernate.lifecycle;

import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.hibernate.SessionFactory;

/**
 * The lifecycle injector is responsible injected and removing lifecycle event listneres.
 *
 * @author kimchy
 */
public interface HibernateEntityLifecycleInjector {

    /**
     * Injects a global lifecycle listener into the concrete <code>SessionFactory<code>
     * implementation.
     *
     * @param sessionFactory The <code>SessionFactory</code> to inject the global lifecycle to.
     * @param device         The Jpa device calling this injector
     * @throws HibernateGpsDeviceException
     */
    void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException;

    /**
     * Removes (if possible) lifecycle listeners injected using the inject method.
     *
     * @param sessionFactory The <code>SessionFactory</code> to remove lifecycle from
     * @param device         The Jpa device calling
     * @throws HibernateGpsDevice
     */
    void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException;
}
