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
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.EntityManagerFactoryImpl;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.compass.core.mapping.ResourceMapping;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * A specilized version that works with OpenJPA. This class should be used instead of
 * {@link org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator} since it works with both openjpa xml files and annotatios.
 *
 * @author kimchy
 */
public class OpenJPAJpaEntitiesLocator implements JpaEntitiesLocator {

    protected Log log = LogFactory.getLog(getClass());

    private ClassLoader classLoader;

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public EntityInformation[] locate(EntityManagerFactory entityManagerFactory, JpaGpsDevice device)
            throws JpaGpsDeviceException {

        CompassGpsInterfaceDevice gps = (CompassGpsInterfaceDevice) device.getGps();

        // TODO this should use OpenJPAEnitiyManagerFactorySPI, here for backward compatability with pre 1.0
        EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl) OpenJPAPersistence.cast(entityManagerFactory);

        ArrayList<EntityInformation> entitiesList = new ArrayList<EntityInformation>();

        // pre initalize the class meta data
        EntityManager entityManager = emf.createEntityManager();
        entityManager.close();

        Collection<Class> classes = emf.getConfiguration().getMetaDataRepositoryInstance().loadPersistentTypes(true, classLoader);
        for (Class clazz : classes) {
            ClassMetaData classMetaData = emf.getConfiguration().getMetaDataRepositoryInstance().getMetaData(clazz, classLoader, true);
            String entityname = classMetaData.getDescribedType().getName();
            if (!gps.hasMappingForEntityForIndex((entityname))) {
                if (log.isDebugEnabled()) {
                    log.debug("Entity [" + entityname + "] does not have compass mapping, filtering it out");
                }
                continue;
            }

            if (shouldFilter(entityname, classMetaData, device)) {
                continue;
            }
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
     * <p>Implementation filteres out inherited OpenJPA mappings, since the select query
     * for the base class will cover any inherited classes as well.
     *
     * <p>Note, that this method is called after it has been verified that the class has
     * Compass mappings (either directly, or indirectly by an interface or a super class).
     *
     * @param entityname    The name of the entity
     * @param classMetadata The OpenJPA class meta data.
     * @param device        The Jpa Gps device
     * @return <code>true</code> if the entity should be filtered out, <code>false</code> if not.
     */
    protected boolean shouldFilter(String entityname, ClassMetaData classMetadata, JpaGpsDevice device) {
        Class<?> clazz = classMetadata.getDescribedType();
        // if it is inherited, do not add it to the classes to index, since the "from [entity]"
        // query for the base class will return results for this class as well
        if (classMetadata.getMappedPCSuperclassMetaData() != null) {
            Class superClass = classMetadata.getMappedPCSuperclassMetaData().getDescribedType();
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
