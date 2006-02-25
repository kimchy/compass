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

import org.compass.gps.CompassGpsInterfaceDevice;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.jpa.entities.JpaEntitiesLocator;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author kimchy
 */
public class HibernateJpaEntitiesLocator implements JpaEntitiesLocator {

    public EntityInformation[] locate(EntityManagerFactory entityManagerFactory, JpaGpsDevice device)
            throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) entityManagerFactory;
        SessionFactory sessionFactory = hibernateEntityManagerFactory.getSessionFactory();

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>();

        Map allClassMetaData = sessionFactory.getAllClassMetadata();
        for (Object o : allClassMetaData.keySet()) {
            String entityname = (String) o;
            if (!gps.hasMappingForEntityForIndex((entityname))) {
                continue;
            }

            ClassMetadata classMetadata = (ClassMetadata) allClassMetaData.get(entityname);
            // if it is inherited, do not add it to the classes to index, since the "from [entity]"
            // query for the base class will return results for this class as well
            if (classMetadata.isInherited()) {
                continue;
            }
            Class<?> clazz = classMetadata.getMappedClass(EntityMode.POJO);
            entitiesList.add(new EntityInformation(clazz, entityname));
        }
        return entitiesList.toArray(new EntityInformation[entitiesList.size()]);
    }
}
