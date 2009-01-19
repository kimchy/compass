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

import javax.persistence.EntityManager;

import org.apache.openjpa.event.DeleteListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.StoreListener;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.compass.core.mapping.Cascade;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * The embedded open JPA event listener listens for after delete/store/persist events and applies them
 * to the index. No transaction handling is perfomed (assumes that it is a managed transaction) and usees
 * {@link org.compass.gps.device.jpa.embedded.openjpa.OpenJPAHelper#getCurrentCompassSession(javax.persistence.EntityManager)}
 * in order to get the actual compass session.
 *
 * @author kimchy
 */
public class EmbeddedOpenJPAEventListener extends AbstractDeviceJpaEntityListener implements DeleteListener, PersistListener, StoreListener {

    private JpaGpsDevice device;

    public EmbeddedOpenJPAEventListener(JpaGpsDevice device) {
        this.device = device;
    }

    @Override
    protected JpaGpsDevice getDevice() {
        return this.device;
    }

    public void beforeDelete(LifecycleEvent lifecycleEvent) {
    }

    public void afterDelete(LifecycleEvent lifecycleEvent) {
        if (disable()) {
            return;
        }
        Object entity = lifecycleEvent.getSource();
        if (!hasMappingForEntity(entity.getClass(), Cascade.DELETE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting [" + entity + "]");
            }
            EntityManager em = OpenJPAPersistence.getEntityManager(entity);
            OpenJPAHelper.getCurrentCompassSession(em).delete(entity);
        } catch (Exception e) {
            log.error("Failed while deleting [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while deleting [" + entity + "]", e);
            }
        }
    }

    public void beforePersist(LifecycleEvent lifecycleEvent) {
    }

    public void afterPersist(LifecycleEvent lifecycleEvent) {
        if (disable()) {
            return;
        }
        Object entity = lifecycleEvent.getSource();
        if (!hasMappingForEntity(entity.getClass(), Cascade.CREATE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating [" + entity + "]");
            }
            EntityManager em = OpenJPAPersistence.getEntityManager(entity);
            OpenJPAHelper.getCurrentCompassSession(em).create(entity);
        } catch (Exception e) {
            log.error("Failed while creating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while creating [" + entity + "]", e);
            }
        }
    }

    public void beforeStore(LifecycleEvent lifecycleEvent) {
    }

    public void afterStore(LifecycleEvent lifecycleEvent) {
        if (disable()) {
            return;
        }
        Object entity = lifecycleEvent.getSource();
        if (!hasMappingForEntity(entity.getClass(), Cascade.SAVE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating [" + entity + "]");
            }
            EntityManager em = OpenJPAPersistence.getEntityManager(entity);
            OpenJPAHelper.getCurrentCompassSession(em).save(entity);
        } catch (Exception e) {
            log.error("Failed while updating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while updating [" + entity + "]", e);
            }
        }
    }

}
