/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.gps.device.jpa.entities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.queryprovider.DefaultJpaQueryProvider;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.datanucleus.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.jpa.EntityManagerFactoryImpl;
import org.datanucleus.metadata.AbstractClassMetaData;

/**
 * @author kimchy
 */
public class DatanucleusJpaEntitiesLocator implements JpaEntitiesLocator {

    protected Log log = LogFactory.getLog(getClass());

    public EntityInformation[] locate(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>();

        JDOPersistenceManagerFactory pmf = extractPMF(entityManagerFactory);
        Collection classNames = pmf.getOMFContext().getMetaDataManager().getClassesWithMetaData();
        for (Iterator it = classNames.iterator(); it.hasNext();) {
            String className = (String) it.next();
            AbstractClassMetaData md = pmf.getOMFContext().getMetaDataManager().getMetaDataForClass(className, pmf.getOMFContext().getClassLoaderResolver(pmf.getPrimaryClassLoader()));

            String entityname = md.getEntityName();
            if (!gps.hasMappingForEntityForIndex((entityname))) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityname + "] does not have compass mapping, filtering it out");
                }
                continue;
            }

            ClassMapping classMapping = (ClassMapping) gps.getMappingForEntityForIndex(entityname);
            // we use FQN of the class for the query since by entity name does not seem to work...
            EntityInformation entityInformation = new EntityInformation(classMapping.getClazz(), entityname, new DefaultJpaQueryProvider("select x from " + classMapping.getClazz().getName() + " x"), classMapping.getSubIndexHash().getSubIndexes());
            entitiesList.add(entityInformation);
            if (log.isDebugEnabled()) {
                log.debug("Entity [" + entityname + "] will be indexed");
            }
        }

        return entitiesList.toArray(new EntityInformation[entitiesList.size()]);
    }

    private static JDOPersistenceManagerFactory extractPMF(EntityManagerFactory emf) {
        Class pmfHolderClass = emf.getClass();
        while (!pmfHolderClass.getName().equals(EntityManagerFactoryImpl.class.getName())) {
            pmfHolderClass = pmfHolderClass.getSuperclass();
            if (pmfHolderClass == Object.class) {
                throw new IllegalStateException("Failed to find PMF from [" + emf.getClass() + "], no [" + EntityManagerFactoryImpl.class.getName() + "] found");
            }
        }
        try {
            Field field = pmfHolderClass.getDeclaredField("pmf");
            field.setAccessible(true);
            return (JDOPersistenceManagerFactory) field.get(emf);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to extract PMF from [" + emf.getClass() + "], no field [pmf]");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to extract PMF from [" + emf.getClass() + "], illegal access");
        }
    }
}
