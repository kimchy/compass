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
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.descriptors.InheritancePolicy;
import oracle.toplink.essentials.ejb.cmp3.EntityManager;
import oracle.toplink.essentials.sessions.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.mapping.ResourceMapping;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * A specilized version that works with TopLink Essentials (Glassfish persistence). This class should be used instead of
 * {@link org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator} since it works with both xml files and annotatios.
 *
 * @author kimchy
 */
public class TopLinkEssentialsJpaEntitiesLocator implements JpaEntitiesLocator {

    protected Log log = LogFactory.getLog(getClass());

    public EntityInformation[] locate(EntityManagerFactory entityManagerFactory, JpaGpsDevice device)
            throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        EntityManager entityManager = (EntityManager) entityManagerFactory.createEntityManager();
        Session session = entityManager.getServerSession();
        entityManager.close();

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>();

        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            String entityname = classDescriptor.getJavaClassName();
            if (!gps.hasMappingForEntityForIndex((entityname))) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityname + "] does not have compass mapping, filtering it out");
                }
                continue;
            }

            if (shouldFilter(entityname, classDescriptor, device)) {
                continue;
            }
            Class<?> clazz = classDescriptor.getJavaClass();
            ResourceMapping resourceMapping = gps.getMappingForEntityForIndex(entityname);
            EntityInformation entityInformation = new EntityInformation(clazz, classDescriptor.getAlias(), resourceMapping.getSubIndexHash().getSubIndexes());
            entitiesList.add(entityInformation);
            if (log.isDebugEnabled()) {
                log.debug("Entity [" + entityname + "] will be indexed");
            }
        }

        return entitiesList.toArray(new EntityInformation[entitiesList.size()]);
    }

    /**
     * Returns <code>true</code> if the entity name needs to be filtered.
     *
     * <p>Implementation filteres out inherited TopLink mappings, since the select query
     * for the base class will cover any inherited classes as well.
     *
     * <p>Note, that this method is called after it has been verified that the class has
     * Compass mappings (either directly, or indirectly by an interface or a super class).
     *
     * @param entityname      The name of the entity
     * @param classDescriptor The TopLink class descriptor
     * @param device          The Jpa Gps device
     * @return <code>true</code> if the entity should be filtered out, <code>false</code> if not.
     */
    protected boolean shouldFilter(String entityname, ClassDescriptor classDescriptor, JpaGpsDevice device) {
        if (!classDescriptor.hasInheritance()) {
            return false;
        }
        InheritancePolicy inheritancePolicy = classDescriptor.getInheritancePolicy();
        if (inheritancePolicy == null || inheritancePolicy.getParentClass() == null) {
            return false;
        }
        Class superClass = inheritancePolicy.getParentClass();
        // only filter out classes that their super class has compass mappings
        if (superClass != null
                && ((CompassGpsInterfaceDevice) device.getGps()).hasMappingForEntityForIndex(superClass)) {
            if (log.isDebugEnabled()) {
                log.debug("Entity [" + entityname + "] is inherited and super class ["
                        + superClass + "] has compass mapping, filtering it out");
            }
            return true;
        }
        return false;
    }
}
