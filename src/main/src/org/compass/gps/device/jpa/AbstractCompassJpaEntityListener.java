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

package org.compass.gps.device.jpa;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.util.ClassUtils;

/**
 * An abstract support class for event lifecycle JPA spec support. Requires the <code>Compass<code>
 * instance to be provided (usual sub classes will simple fetch it from the Jndi location). This
 * is the least prefereable way to use lifecycle event listerens, please see
 * {@link JpaGpsDevice} and {@link org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjector}.
 *
 * @author kimchy
 */
public abstract class AbstractCompassJpaEntityListener {

    protected Log log = LogFactory.getLog(getClass());

    protected abstract Compass getCompass();

    private CompassTemplate compassTemplate;

    /**
     * Should exception be thrown during the mirroring operation, or just logged.
     * Defaults to <code>true</code>.
     */
    protected boolean throwExceptionOnError() {
        return true;
    }

    protected boolean disable() {
        return false;
    }

    protected boolean hasMappingForEntity(Class clazz, Cascade cascade) {
        ResourceMapping resourceMapping = ((InternalCompass) getCompass()).getMapping().getMappingByClass(clazz);
        if (resourceMapping == null) {
            return false;
        }
        if (resourceMapping.isRoot()) {
            return true;
        }
        return resourceMapping.operationAllowed(cascade);
    }

    protected boolean hasMappingForEntity(String name, Cascade cascade) {
        ResourceMapping resourceMapping = ((InternalCompass) getCompass()).getMapping().getMappingByAlias(name);
        if (resourceMapping != null) {
            if (resourceMapping.isRoot()) {
                return true;
            }
            return resourceMapping.operationAllowed(cascade);
        }
        try {
            Class clazz = ClassUtils.forName(name, getCompass().getSettings().getClassLoader());
            resourceMapping = ((InternalCompass) getCompass()).getMapping().getMappingByClass(clazz);
            if (resourceMapping.isRoot()) {
                return true;
            }
            return resourceMapping.operationAllowed(cascade);
        } catch (Exception e) {
            // do nothing
        }
        return false;
    }

    @PostPersist
    public void postPersist(final Object entity) throws CompassException {
        if (disable()) {
            return;
        }
        if (!hasMappingForEntity(entity.getClass(), Cascade.CREATE)) {
            return;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating [" + entity + "]");
            }
            getCompassTemplate().execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    session.create(entity);
                }
            });
        } catch (Exception e) {
            log.error("Failed while creating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while creating [" + entity + "]", e);
            }
        }
    }

    @PostUpdate
    public void postUpdate(final Object entity) throws CompassException {
        if (disable()) {
            return;
        }
        if (!hasMappingForEntity(entity.getClass(), Cascade.SAVE)) {
            return;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating [" + entity + "]");
            }
            getCompassTemplate().execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    session.save(entity);
                }
            });
        } catch (Exception e) {
            log.error("Failed while updating [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while unpdating [" + entity + "]", e);
            }
        }
    }

    @PostRemove
    public void postRemove(final Object entity) throws CompassException {
        if (disable()) {
            return;
        }
        if (!hasMappingForEntity(entity.getClass(), Cascade.DELETE)) {
            return;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing [" + entity + "]");
            }
            getCompassTemplate().execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    session.delete(entity);
                }
            });
        } catch (Exception e) {
            log.error("Failed while removing [" + entity + "]", e);
            if (throwExceptionOnError()) {
                throw new JpaGpsDeviceException("Failed while removing [" + entity + "]", e);
            }
        }
    }

    private CompassTemplate getCompassTemplate() {
        if (compassTemplate == null) {
            compassTemplate = new CompassTemplate(getCompass());
        }
        return compassTemplate;
    }

}
