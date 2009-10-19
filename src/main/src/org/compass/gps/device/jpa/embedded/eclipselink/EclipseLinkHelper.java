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

package org.compass.gps.device.jpa.embedded.eclipselink;

import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.embedded.JpaCompassGps;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.Session;

/**
 * Helper class to get different Compass constructs embedded with EclipseLink.
 *
 * @author kimchy
 */
public abstract class EclipseLinkHelper {

    private EclipseLinkHelper() {

    }

    /**
     * Returns the Compass instance assoicated with the given EclipseLink {@link javax.persistence.EntityManagerFactory}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getCompass(em);
        } finally {
            em.close();
        }
    }

    /**
     * Returns the Compass instance assoicated with the given EclispeLink {@link javax.persistence.EntityManager}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManager em) {
        return findCompassSessionEventListener(em).getCompass();
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManagerFactory}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instance.
     */
    public static Properties getIndexSettings(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getIndexSettings(em);
        } finally {
            em.close();
        }
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManager}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instnace.
     */
    public static Properties getIndexSettings(EntityManager em) {
        return findCompassSessionEventListener(em).getIndexSettings();
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
        JpaEntityManager nativeEm = (JpaEntityManager) NativeJpaHelper.extractNativeJpa(em);
        return findCompassSessionEventListener(nativeEm.getServerSession()).getCurrentCompassSession(nativeEm.getUnitOfWork());
    }

    /**
     * Returns the current Compass session associated with the {@link javax.persistence.EntityManager}.
     * Compass Session is associated with an Entity Manager when a transaction is started and removed when the
     * transaction commits/rollsback.
     *
     * <p>The session can be used to perform searches that needs to take into account current transactional changes
     * or to perform additional Compass operations that are not reflected by the mirroring feature.
     */
    public static CompassSession getCurrentCompassSession(Session session) {
        return findCompassSessionEventListener(session).getCurrentCompassSession(session);
    }

    /**
     * Returns the Compass Gps instance associated with the given EclipseLink {@link javax.persistence.EntityManagerFactory}.
     * Used in order to perform {@link org.compass.gps.device.jpa.embedded.JpaCompassGps#index()} operation. Note, the index
     * operation should not be perfomed within a running transaction.
     */
    public static JpaCompassGps getCompassGps(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getCompassGps(em);
        } finally {
            em.close();
        }
    }

    /**
     * Returns the Compass Gps instance associated with the given EclipseLink {@link javax.persistence.EntityManager}.
     * Used in order to perform {@link org.compass.gps.device.jpa.embedded.JpaCompassGps#index()} operation. Note, the index
     * operation should not be perfomed within a running transaction.
     */
    public static JpaCompassGps getCompassGps(EntityManager em) {
        return findCompassSessionEventListener(em).getJpaCompassGps();
    }

    /**
     * A helper class to create the <code>JpaCompasGps</code> based on the passed device.
     */
    public static JpaCompassGps createCompassGps(JpaGpsDevice device) {
        DefaultJpaCompassGps gps = new DefaultJpaCompassGps(getCompass(device.getEntityManagerFactory()));
        device.setMirrorDataChanges(false);
        gps.setIndexProperties(getIndexSettings(device.getEntityManagerFactory()));
        gps.addGpsDevice(device);
        return gps;
    }

    private static CompassSessionEventListener findCompassSessionEventListener(EntityManager em) throws CompassException {
        return findCompassSessionEventListener(((JpaEntityManager) NativeJpaHelper.extractNativeJpa(em)).getServerSession());
    }

    private static CompassSessionEventListener findCompassSessionEventListener(Session session) throws CompassException {
        List listeners = session.getEventManager().getListeners();
        for (Object o : listeners) {
            if (o instanceof CompassSessionEventListener) {
                return (CompassSessionEventListener) o;
            }
        }
        throw new CompassException("Failed to find CompassSessionEventListener, have you configured Compass with EclipseLink?");
    }
}