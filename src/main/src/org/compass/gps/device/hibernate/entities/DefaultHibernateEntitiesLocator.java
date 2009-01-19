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

package org.compass.gps.device.hibernate.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.mapping.ResourceMapping;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;

/**
 * A default implemenation that automatically locates entities to index based
 * on Hibernate current mapped objects and mappings for the objects that exists
 * with Compass.
 *
 * @author kimchy
 */
public class DefaultHibernateEntitiesLocator implements HibernateEntitiesLocator {

    protected Log log = LogFactory.getLog(getClass());

    public EntityInformation[] locate(SessionFactory sessionFactory, HibernateGpsDevice device)
            throws HibernateGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>();

        Map allClassMetaData = sessionFactory.getAllClassMetadata();
        for (Iterator it = allClassMetaData.keySet().iterator(); it.hasNext();) {
            String entityname = (String) it.next();
            if (!gps.hasMappingForEntityForIndex((entityname))) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityname + "] does not have compass mapping, filtering it out");
                }
                continue;
            }

            ClassMetadata classMetadata = (ClassMetadata) allClassMetaData.get(entityname);
            if (shouldFilter(entityname, classMetadata, allClassMetaData, device)) {
                continue;
            }
            Class clazz = classMetadata.getMappedClass(EntityMode.POJO);
            ResourceMapping resourceMapping = gps.getMappingForEntityForIndex(entityname);
            EntityInformation entityInformation = new EntityInformation(clazz, entityname, resourceMapping.getSubIndexHash().getSubIndexes());
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
     * <p>Implementation filteres out inherited hibernate mappings, since the select query
     * for the base class will cover any inherited classes as well.
     *
     * <p>Note, that this method is called after it has been verified that the class has
     * Compass mappings (either directly, or indirectly by an interface or a super class).
     *
     * @param entityname    The name of the entity
     * @param classMetadata The Hibernate class meta data.
     * @param device        The Hibernate Gps device
     * @return <code>true</code> if the entity should be filtered out, <code>false</code> if not.
     */
    protected boolean shouldFilter(String entityname, ClassMetadata classMetadata, Map allClassMetaData,
                                   HibernateGpsDevice device) {
        Class clazz = classMetadata.getMappedClass(EntityMode.POJO);
        // if it is inherited, do not add it to the classes to index, since the "from [entity]"
        // query for the base class will return results for this class as well
        if (classMetadata.isInherited()) {
            String superClassEntityName = ((AbstractEntityPersister) classMetadata).getMappedSuperclass();
            ClassMetadata superClassMetadata = (ClassMetadata) allClassMetaData.get(superClassEntityName);
            Class superClass = superClassMetadata.getMappedClass(EntityMode.POJO);
            // only filter out classes that their super class has compass mappings
            if (superClass != null
                    && ((CompassGpsInterfaceDevice) device.getGps()).hasMappingForEntityForIndex(superClass)) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityname + "] is inherited and super class ["
                            + superClass + "] has compass mapping, filtering it out");
                }
                return true;
            }
        }
        return false;
    }
}