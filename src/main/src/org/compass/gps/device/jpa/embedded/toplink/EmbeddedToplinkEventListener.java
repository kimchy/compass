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

package org.compass.gps.device.jpa.embedded.toplink;

import java.util.Vector;

import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventListener;
import oracle.toplink.essentials.sessions.Session;
import org.compass.core.mapping.Cascade;
import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

/**
 * An event listener that mirrors changes done through toplink to Compass when using TopLink embedded support.
 * Uses {@link org.compass.gps.device.jpa.embedded.toplink.TopLinkHelper#getCurrentCompassSession(oracle.toplink.essentials.sessions.Session)}
 * in order to get the current {@link org.compass.core.CompassSession} and perform with it the relevant mirror operations.
 *
 * @author kimchy
 */
public class EmbeddedToplinkEventListener extends AbstractDeviceJpaEntityListener implements DescriptorEventListener {

    private JpaGpsDevice device;

    public EmbeddedToplinkEventListener(JpaGpsDevice device) {
        this.device = device;
    }

    @Override
    protected JpaGpsDevice getDevice() {
        return this.device;
    }

    public void postUpdate(DescriptorEvent event) {
        if (disable()) {
            return;
        }
        Object entity = event.getObject();
        if (!hasMappingForEntity(entity.getClass(), Cascade.SAVE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating [" + entity + "]");
            }
            Session session = event.getSession();
            TopLinkHelper.getCurrentCompassSession(session).save(entity);
        } catch (Exception e) {
            log.error("Failed while updating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while updating [" + entity + "]", e);
            }
        }
    }

    public void postDelete(DescriptorEvent event) {
        if (disable()) {
            return;
        }
        Object entity = event.getObject();
        if (!hasMappingForEntity(entity.getClass(), Cascade.DELETE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting [" + entity + "]");
            }
            Session session = event.getSession();
            TopLinkHelper.getCurrentCompassSession(session).delete(entity);
        } catch (Exception e) {
            log.error("Failed while deleting [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while deleting [" + entity + "]", e);
            }
        }
    }

    public void postInsert(DescriptorEvent event) {
        if (disable()) {
            return;
        }
        Object entity = event.getObject();
        if (!hasMappingForEntity(entity.getClass(), Cascade.CREATE)) {
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating [" + entity + "]");
            }
            Session session = event.getSession();
            TopLinkHelper.getCurrentCompassSession(session).create(entity);
        } catch (Exception e) {
            log.error("Failed while creating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while creating [" + entity + "]", e);
            }
        }
    }

    // things we don't use

    public void aboutToDelete(DescriptorEvent event) {
    }

    public void aboutToInsert(DescriptorEvent event) {
    }

    public void aboutToUpdate(DescriptorEvent event) {
    }

    public boolean isOverriddenEvent(DescriptorEvent event, Vector eventManagers) {
        return false;
    }

    public void postBuild(DescriptorEvent event) {
    }

    public void postClone(DescriptorEvent event) {
    }

    public void postMerge(DescriptorEvent event) {
    }

    public void postRefresh(DescriptorEvent event) {
    }

    public void postWrite(DescriptorEvent event) {
    }

    public void preDelete(DescriptorEvent event) {
    }

    public void preInsert(DescriptorEvent event) {
    }

    public void prePersist(DescriptorEvent event) {
    }

    public void preRemove(DescriptorEvent event) {
    }

    public void preUpdate(DescriptorEvent event) {
    }

    public void preUpdateWithChanges(DescriptorEvent event) {
    }

    public void preWrite(DescriptorEvent event) {
    }

}
