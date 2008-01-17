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

package org.compass.gps.device.jpa.embedded.toplink;

import java.util.Properties;
import java.util.Vector;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.sessions.Session;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.embedded.JpaCompassGps;
import org.compass.gps.device.support.parallel.SameThreadParallelIndexExecutor;

/**
 * @author kimchy
 */
public abstract class TopLinkHelper {

    private TopLinkHelper() {

    }

    public static Compass getCompass(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getCompass(em);
        } finally {
            em.close();
        }
    }

    public static Compass getCompass(EntityManager em) {
        return findCompassSessionEventListener(em).getCompass();
    }

    public static Properties getIndexSettings(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getIndexSettings(em);
        } finally {
            em.close();
        }
    }

    public static Properties getIndexSettings(EntityManager em) {
        return findCompassSessionEventListener(em).getIndexSettings();
    }

    public static CompassSession getCurrentCompassSession(EntityManager em) {
        Session serverSession = ((oracle.toplink.essentials.ejb.cmp3.EntityManager) em).getServerSession();
        Session session = ((oracle.toplink.essentials.ejb.cmp3.EntityManager) em).getUnitOfWork();
        return findCompassSessionEventListener(serverSession).getCurrentCompassSession(session);
    }

    public static CompassSession getCurrentCompassSession(Session session) {
        return findCompassSessionEventListener(session).getCurrentCompassSession(session);
    }

    public static JpaCompassGps getCompassGps(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            return getCompassGps(em);
        } finally {
            em.close();
        }
    }

    public static JpaCompassGps getCompassGps(EntityManager em) {
        return findCompassSessionEventListener(em).getJpaCompassGps();
    }

    public static JpaCompassGps createCompassGps(EntityManagerFactory emf) {
        JpaGpsDevice device = new JpaGpsDevice("jpadevice", emf);
        return createCompassGps(device);
    }

    public static JpaCompassGps createCompassGps(JpaGpsDevice device) {
        DefaultJpaCompassGps gps = new DefaultJpaCompassGps(getCompass(device.getEntityManagerFactory()));
        device.setMirrorDataChanges(false);
        device.setParallelIndexExecutor(new SameThreadParallelIndexExecutor());
        gps.setIndexProperties(getIndexSettings(device.getEntityManagerFactory()));
        gps.addGpsDevice(device);
        gps.start();
        return gps;
    }

    private static CompassSessionEventListener findCompassSessionEventListener(EntityManager em) throws CompassException {
        return findCompassSessionEventListener(((oracle.toplink.essentials.ejb.cmp3.EntityManager) em).getServerSession());
    }

    private static CompassSessionEventListener findCompassSessionEventListener(Session session) throws CompassException {
        Vector listeners = session.getEventManager().getListeners();
        for (Object o : listeners) {
            if (o instanceof CompassSessionEventListener) {
                return (CompassSessionEventListener) o;
            }
        }
        throw new CompassException("Failed to find CompassSessionEventListener, have you configured Compass with TopLink?");
    }
}
