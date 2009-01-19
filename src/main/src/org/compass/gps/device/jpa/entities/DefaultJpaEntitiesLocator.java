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

package org.compass.gps.device.jpa.entities;

import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Inheritance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * The default {@link JpaEntitiesLocator} implementations. Should work with all different
 * JPA implementations, with the only caveat that it is based on Annotations only. So, any
 * applications that uses a combination of both annotations and xml definitions (or any other
 * type), will probably loose some information during the index process (providing that the
 * xml definitions are ones that collide with Compass definitions). Please check if a
 * JPA actual implementation is provided with compass in such cases, and if not, writing
 * one should be simple (and should probably use the actual JPA implementation APIs).
 *
 * @author kimchy
 */
public class DefaultJpaEntitiesLocator implements JpaEntitiesLocator {

    protected Log log = LogFactory.getLog(getClass());

    public EntityInformation[] locate(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();
        final ResourceMapping[] resourceMappings =
                ((InternalCompass) gps.getIndexCompass()).getMapping().getRootMappings();

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>(resourceMappings.length);
        for (ResourceMapping resourceMapping : resourceMappings) {
            if (!(resourceMapping instanceof ClassMapping)) {
                continue;
            }
            ClassMapping classMapping = (ClassMapping) resourceMapping;
            if (!classMapping.isRoot()) {
                continue;
            }
            EntityInformation entityInformation = createEntityInformation(classMapping.getClazz(), resourceMapping);
            if (entityInformation == null) {
                continue;
            }
            if (shouldFilter(entityInformation, device)) {
                continue;
            }
            entitiesList.add(entityInformation);
        }
        return entitiesList.toArray(new EntityInformation[entitiesList.size()]);
    }

    /**
     * Creates the {@link EntityInformation} for a given class. If return <code>null</code>
     * the class will be filtered out.
     * <p/>
     * Implementation filters out classes that do not have the {@link Entity} annotation (i.e.
     * return <code>null</code> for such a case).
     *
     * @param clazz           The class to create the {@link EntityInformation} for
     * @param resourceMapping The Compass resource mapping (used for sub indexes extraction)
     * @return The entity information, or <code>null</code> to filter it out
     * @throws JpaGpsDeviceException
     */
    protected EntityInformation createEntityInformation(Class<?> clazz, ResourceMapping resourceMapping)
            throws JpaGpsDeviceException {
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity == null) {
            return null;
        }
        String name;
        if (StringUtils.hasLength(entity.name())) {
            name = entity.name();
        } else {
            name = ClassUtils.getShortName(clazz);
        }
        return new EntityInformation(clazz, name, resourceMapping.getSubIndexHash().getSubIndexes());
    }

    /**
     * Return <code>true</code> if the entity should be filtered out from the index operation.
     * <p/>
     * Implementation filters out classes that one of the super classes has the {@link Inheritance}
     * annotation and the super class has compass mappings.
     *
     * @param entityInformation The entity information to check if it should be filtered
     * @param device            The Jpa gps device
     * @return <code>true</code> if the entity should be filtered from the index process
     */
    protected boolean shouldFilter(EntityInformation entityInformation, JpaGpsDevice device) {
        Class<?> clazz = entityInformation.getEntityClass().getSuperclass();
        while (true) {
            if (clazz == null || clazz.equals(Object.class)) {
                break;
            }
            if (clazz.isAnnotationPresent(Inheritance.class)
                    && ((CompassGpsInterfaceDevice) device.getGps()).hasMappingForEntityForIndex(clazz)) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityInformation.getName()
                            + "] is inherited and super class [" + clazz + "] has compass mapping, filtering it out");
                }
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}
