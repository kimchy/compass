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

package org.compass.gps.device.jpa.hibernate;

import org.compass.gps.device.jpa.AbstractSimpleJpaGpsDeviceTests;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.model.Simple;
import org.compass.gps.device.jpa.model.SimpleBase;
import org.compass.gps.device.jpa.model.SimpleExtend;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.Ejb3Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * @author kimchy
 */
public class HibernateSimpleJpaGpsDeviceTests extends AbstractSimpleJpaGpsDeviceTests {

    @Override
    protected void addDeviceSettings(JpaGpsDevice device) {
        device.setInjectEntityLifecycleListener(true);
    }

    protected EntityManagerFactory doSetUpEntityManagerFactory() {
        Ejb3Configuration cfg = new Ejb3Configuration()
                .configure("/org/compass/gps/device/jpa/hibernate/hibernate3.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create")
                .addAnnotatedClass(Simple.class)
                .addAnnotatedClass(SimpleBase.class)
                .addAnnotatedClass(SimpleExtend.class);
        return cfg.createEntityManagerFactory();
    }
}
