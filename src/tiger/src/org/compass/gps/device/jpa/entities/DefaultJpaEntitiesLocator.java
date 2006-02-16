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

package org.compass.gps.device.jpa.entities;

import org.compass.core.impl.InternalCompass;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;
import org.compass.gps.CompassGpsInterfaceDevice;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Inheritance;
import java.util.ArrayList;

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
            EntityInformation entityInformation = createEntityInformation(classMapping.getClazz());
            if (entityInformation == null) {
                continue;
            }
            if (!shouldFilter(entityInformation)) {
                entitiesList.add(entityInformation);
            }
        }
        return entitiesList.toArray(new EntityInformation[entitiesList.size()]);
    }

    protected EntityInformation createEntityInformation(Class<?> clazz) throws JpaGpsDeviceException {
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
        return new EntityInformation(clazz, name);
    }

    protected boolean shouldFilter(EntityInformation entityInformation) {
        boolean hasIneritence = false;
        Class<?> clazz = entityInformation.getClazz().getSuperclass();
        while (true) {
            if (clazz == null || clazz.equals(Object.class)) {
                break;
            }
            if (clazz.isAnnotationPresent(Inheritance.class)) {
                hasIneritence = true;
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return hasIneritence;
    }
}
