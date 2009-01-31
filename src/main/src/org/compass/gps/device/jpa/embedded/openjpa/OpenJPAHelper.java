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

package org.compass.gps.device.jpa.embedded.openjpa;

import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.embedded.JpaCompassGps;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;

/**
 * Helper class to get different Compass constructs embedded with Open JPA.
 *
 * @author kimchy
 */
public abstract class OpenJPAHelper {

    private OpenJPAHelper() {

    }

    /**
     * Returns the Compass instance assoicated with the given OpenJPA {@link javax.persistence.EntityManagerFactory}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        OpenJPAEntityManagerFactory openJpaEmf = OpenJPAPersistence.cast(nativeEmf);
        return (Compass) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_USER_OBJECT_KEY);
    }

    /**
     * Returns the Compass instance assoicated with the given OpenJPA {@link javax.persistence.EntityManager}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        OpenJPAEntityManagerFactory openJpaEmf =
                OpenJPAPersistence.cast(nativeEm).getEntityManagerFactory();
        return (Compass) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_USER_OBJECT_KEY);
    }

    /**
     * Returns the Compass Gps instance associated with the given OpenJPA {@link javax.persistence.EntityManagerFactory}.
     * Used in order to perform {@link org.compass.gps.device.jpa.embedded.JpaCompassGps#index()} operation. Note, the index
     * operation should not be perfomed within a running transaction.
     */
    public static JpaCompassGps getCompassGps(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        OpenJPAEntityManagerFactory openJpaEmf = OpenJPAPersistence.cast(nativeEmf);
        return (JpaCompassGps) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_GPS_USER_OBJECT_KEY);
    }

    /**
     * Returns the Compass Gps instance associated with the given OpenJPA {@link javax.persistence.EntityManager}.
     * Used in order to perform {@link org.compass.gps.device.jpa.embedded.JpaCompassGps#index()} operation. Note, the index
     * operation should not be perfomed within a running transaction.
     */
    public static JpaCompassGps getCompassGps(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        OpenJPAEntityManagerFactory openJpaEmf =
                OpenJPAPersistence.cast(nativeEm).getEntityManagerFactory();
        return (JpaCompassGps) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_GPS_USER_OBJECT_KEY);
    }

    /**
     * Returns the current Compass session associated with the {@link javax.persistence.EntityManager}.
     * Compass Session is associated with an Entity Manager when a transaction is started and removed when the
     * transaction commits/rollsback.
     *
     * <p>The session can be used to perform searches that needs to take into account current transactional changes
     * or to perform additional Compass operations that are not reflected by the mirroring feature.
     */
    public static CompassSession getCurrentCompassSession(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        OpenJPAEntityManager openJPAEntityManager = OpenJPAPersistence.cast(nativeEm);
        return (CompassSession) openJPAEntityManager.getUserObject(CompassProductDerivation.COMPASS_SESSION_USER_OBJECT_KEY);
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManagerFactory}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instance.
     */
    public Properties getIndexSettings(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        OpenJPAEntityManagerFactory openJpaEmf = OpenJPAPersistence.cast(nativeEmf);
        return (Properties) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_INDEX_SETTINGS_USER_OBJECT_KEY);
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManager}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instnace.
     */
    public Properties getIndexSettings(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        OpenJPAEntityManagerFactory openJpaEmf =
                OpenJPAPersistence.cast(nativeEm).getEntityManagerFactory();
        return (Properties) openJpaEmf.getUserObject(CompassProductDerivation.COMPASS_INDEX_SETTINGS_USER_OBJECT_KEY);
    }
}
