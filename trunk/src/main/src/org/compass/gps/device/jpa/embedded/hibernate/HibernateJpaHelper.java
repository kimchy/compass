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
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernateEntityManagerFactory;

/**
 * @author kimchy
 */
public abstract class HibernateJpaHelper {

    private HibernateJpaHelper() {

    }

    public static Compass getCompass(EntityManagerFactory emf) {
        return HibernateHelper.getCompass(((HibernateEntityManagerFactory) emf).getSessionFactory());
    }

    public static CompassTemplate getCompassTemplate(EntityManagerFactory emf) {
        return HibernateHelper.getCompassTempalte(((HibernateEntityManagerFactory) emf).getSessionFactory());
    }

    public static Compass getCompass(EntityManager em) {
        return HibernateHelper.getCompass(((HibernateEntityManager) em).getSession());
    }

    public static CompassTemplate getCompassTemplate(EntityManager em) {
        return HibernateHelper.getCompassTempalte(((HibernateEntityManager) em).getSession());
    }

    public static Properties getIndexSettings(EntityManagerFactory emf) {
        return HibernateHelper.getIndexSettings(((HibernateEntityManagerFactory) emf).getSessionFactory());
    }

    public static Properties getIndexSettings(EntityManager em) {
        return HibernateHelper.getIndexSettings(((HibernateEntityManager) em).getSession());
    }

    public static JpaCompassGps getCompassGps(EntityManagerFactory emf) {
        JpaGpsDevice device = new JpaGpsDevice("jpadevice", emf);
        return getCompassGps(device);
    }

    public static JpaCompassGps getCompassGps(JpaGpsDevice device) {
        DefaultJpaCompassGps gps = new DefaultJpaCompassGps(getCompass(device.getEntityManagerFactory()));
        device.setMirrorDataChanges(false);
        gps.setIndexProperties(getIndexSettings(device.getEntityManagerFactory()));
        gps.addGpsDevice(device);
        gps.start();
        return gps;
    }
}
