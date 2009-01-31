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

package org.compass.gps.device.jpa.embedded.hibernate;

import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.compass.gps.device.hibernate.embedded.HibernateHelper;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.embedded.JpaCompassGps;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernateEntityManagerFactory;

/**
 * A helper class allowing to get {@link org.compass.core.Compass} and {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
 * when working with Hibernate JPA in an embedded mode.
 *
 * @author kimchy
 */
public abstract class HibernateJpaHelper {

    /**
     * Returns the Compass instance assoicated with the given Hibernate {@link javax.persistence.EntityManagerFactory}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        return HibernateHelper.getCompass(((HibernateEntityManagerFactory) nativeEmf).getSessionFactory());
    }

    /**
     * Returns the CompassTemplate instance assoicated with the given Hibernate {@link javax.persistence.EntityManagerFactory}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static CompassTemplate getCompassTemplate(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        return HibernateHelper.getCompassTempalte(((HibernateEntityManagerFactory) nativeEmf).getSessionFactory());
    }

    /**
     * Returns the Compass instance assoicated with the given OpenJPA {@link javax.persistence.EntityManager}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static Compass getCompass(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        return HibernateHelper.getCompass(((HibernateEntityManager) nativeEm).getSession());
    }

    /**
     * Returns the CompassTemplate instance assoicated with the given OpenJPA {@link javax.persistence.EntityManager}.
     * This allows to get a Compass instnace in order to perform search operations for example outside of a JPA
     * transaction (for performance reasons, mostly there is no need to start a DB transaction).
     */
    public static CompassTemplate getCompassTemplate(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        return HibernateHelper.getCompassTempalte(((HibernateEntityManager) nativeEm).getSession());
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManagerFactory}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instance.
     */
    public static Properties getIndexSettings(EntityManagerFactory emf) {
        EntityManagerFactory nativeEmf = NativeJpaHelper.extractNativeJpa(emf);
        return HibernateHelper.getIndexSettings(((HibernateEntityManagerFactory) nativeEmf).getSessionFactory());
    }

    /**
     * Returns the index settings that are configured within the {@link javax.persistence.EntityManager}
     * configuration. Can be used to configure exteranally a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps}
     * instnace.
     */
    public static Properties getIndexSettings(EntityManager em) {
        EntityManager nativeEm = NativeJpaHelper.extractNativeJpa(em);
        return HibernateHelper.getIndexSettings(((HibernateEntityManager) nativeEm).getSession());
    }

    /**
     * Returns a new instnacoef of a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps} built on top
     * of the embedded {@link org.compass.core.Compass} instance.
     */
    public static JpaCompassGps getCompassGps(EntityManagerFactory emf) {
        JpaGpsDevice device = new JpaGpsDevice("jpadevice", emf);
        return getCompassGps(device);
    }

    /**
     * Returns a new instnacoef of a {@link org.compass.gps.device.jpa.embedded.JpaCompassGps} built on top
     * of the embedded {@link org.compass.core.Compass} instance.
     */
    public static JpaCompassGps getCompassGps(JpaGpsDevice device) {
        DefaultJpaCompassGps gps = new DefaultJpaCompassGps(getCompass(device.getEntityManagerFactory()));
        device.setMirrorDataChanges(false);
        gps.setIndexProperties(getIndexSettings(device.getEntityManagerFactory()));
        gps.addGpsDevice(device);
        gps.start();
        return gps;
    }

    private HibernateJpaHelper() {

    }

}
