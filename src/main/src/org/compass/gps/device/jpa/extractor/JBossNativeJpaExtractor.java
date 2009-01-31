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
import org.jboss.ejb3.entity.InjectedEntityManagerFactory;

/**
 * @author kimchy
 */
public class JBossNativeJpaExtractor implements NativeJpaExtractor {

    /**
     * Extracts the native entity manager factory from a managed JBoss one. If JBoss
     * has not wrapped the factory, will return it as is.
     *
     * @param entityManagerFactory The (possibly) managed JBoss entity manager factory
     * @return The native entity manager factory
     * @throws org.compass.gps.device.jpa.JpaGpsDeviceException
     *
     */
    public EntityManagerFactory extractNative(EntityManagerFactory entityManagerFactory) throws JpaGpsDeviceException {
        if (entityManagerFactory instanceof InjectedEntityManagerFactory) {
            return ((InjectedEntityManagerFactory) entityManagerFactory).getDelegate();
        }
        return entityManagerFactory;
    }

    public EntityManager extractNative(EntityManager entityManager) throws JpaGpsDeviceException {
        return entityManager;
    }
}
