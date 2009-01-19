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

package org.compass.gps.device.jpa.embedded;

import javax.persistence.EntityManagerFactory;

import org.compass.core.Compass;
import org.compass.gps.device.jpa.embedded.eclipselink.EclipseLinkHelper;
import org.compass.gps.device.jpa.embedded.hibernate.HibernateJpaHelper;
import org.compass.gps.device.jpa.embedded.openjpa.OpenJPAHelper;
import org.compass.gps.device.jpa.embedded.toplink.TopLinkHelper;

/**
 * The generic embedded JPA helper can extract a {@link Compass} and a {@link JpaCompassGps}
 * out of either Hibernate JPA, OpenJPA, TopLink or EclipseLink by identifying it automatically.
 *
 * @author kimchy
 */
public class GenericJpaHelper {

    private EntityManagerFactory entityManagerFactory;

    private Compass compass;

    private JpaCompassGps jpaCompassGps;

    /**
     * Constructs a new helper and tries to autmoatically identify from which JPA provide
     * the embedded Compass can be extracted from.
     *
     * @param entityManagerFactory The entity manager factory to get the Compass instance from
     */
    public GenericJpaHelper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        try {
            compass = HibernateJpaHelper.getCompass(entityManagerFactory);
            jpaCompassGps = HibernateJpaHelper.getCompassGps(entityManagerFactory);
        } catch (Throwable t) {
            // do nothing
        }
        if (compass != null) {
            return;
        }
        try {
            compass = OpenJPAHelper.getCompass(entityManagerFactory);
            jpaCompassGps = OpenJPAHelper.getCompassGps(entityManagerFactory);
        } catch (Throwable t) {
            // do nothing
        }
        if (compass != null) {
            return;
        }
        try {
            compass = TopLinkHelper.getCompass(entityManagerFactory);
            jpaCompassGps = TopLinkHelper.getCompassGps(entityManagerFactory);
        } catch (Throwable t) {
            // do nothing
        }
        if (compass != null) {
            return;
        }
        try {
            compass = EclipseLinkHelper.getCompass(entityManagerFactory);
            jpaCompassGps = EclipseLinkHelper.getCompassGps(entityManagerFactory);
        } catch (Throwable t) {
            // do nothing
        }
        if (compass != null) {
            return;
        }
        if (compass == null) {
            throw new IllegalStateException("Failed to find embedded Compass in OpenJPA/Hiberante/TopLink/EclipseLink");
        }
    }

    /**
     * Returns the EMF provided.
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * Returns the {@link Compass} instnace embedded within the JPA EMF.
     */
    public Compass getCompass() {
        return this.compass;
    }

    /**
     * Returns the {@link JpaCompassGps} instnace embedded within the JPA EMF.
     */
    public JpaCompassGps getJpaCompassGps() {
        return jpaCompassGps;
    }
}
