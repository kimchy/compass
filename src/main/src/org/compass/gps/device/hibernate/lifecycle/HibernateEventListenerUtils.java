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

package org.compass.gps.device.hibernate.lifecycle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.transaction.Synchronization;

import org.compass.core.CompassSession;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.util.Assert;
import org.compass.core.util.FieldInvoker;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.EntityMode;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

/**
 * @author kimchy
 */
public abstract class HibernateEventListenerUtils {

    public static void registerRemovalHook(EventSource eventSource, Map<Object, Collection> pendingMap, Object entity) {
        eventSource.getTransaction().registerSynchronization(new RemoveFromPending(pendingMap, entity));
    }


    public static Collection getAssociatedDependencies(Object entity, Map<Object, Collection> pendingMap) {
        Set dependencies = new HashSet();
        for (Collection other : pendingMap.values()) {
            if (other.contains(entity)) {
                dependencies.addAll(other);
            }
        }
        return dependencies;
    }

    public static Collection getUnpersistedCascades(CompassGpsInterfaceDevice compassGps, Object entity,
                                                    SessionFactoryImplementor sessionFactory, Cascade cascade,
                                                    Collection visited) {
        if (visited.contains(entity)) {
            return Collections.EMPTY_SET;
        }
        visited.add(entity);

        ClassMetadata classMetadata = sessionFactory.getClassMetadata(entity.getClass());
        if (classMetadata == null) {
            for (Iterator iter = sessionFactory.getAllClassMetadata().values().iterator(); iter.hasNext();) {
                ClassMetadata temp = (ClassMetadata) iter.next();
                if (entity.getClass().equals(temp.getMappedClass(EntityMode.POJO))) {
                    classMetadata = temp;
                    break;
                }
            }
        }
        Assert.notNull(classMetadata, "Failed to lookup Hibernate ClassMetadata for entity [" + entity + "]");
        String entityName = classMetadata.getEntityName();
        EntityPersister persister = sessionFactory.getEntityPersister(entityName);

        CompassMapping compassMapping = ((InternalCompass) compassGps.getIndexCompass()).getMapping();
        ClassMapping classMapping = (ClassMapping) compassMapping.getMappingByClass(entity.getClass());
        if (classMapping == null) {
            return Collections.EMPTY_SET;
        }

//        CascadeStyle[] cascadeStyles = persister.getEntityMetamodel().getCascadeStyles();
        String[] propertyNames = persister.getPropertyNames();
        Type[] types = persister.getPropertyTypes();
        Set dependencies = new HashSet();
        for (int i = 0, len = propertyNames.length; i < len; i++) {
            // property cascade includes save/update?
//            CascadeStyle cascadeStyle = cascadeStyles[i];
//            if (!cascadeStyle.doCascade(CascadingAction.SAVE_UPDATE)) {
//                continue;
//            }
            // property is mapped in Compass?
            String name = propertyNames[i];
            Mapping mapping = classMapping.getMapping(name);
            if (mapping == null) {
                continue;
            }
            // property value is not null?
            Object propertyValue = persister.getPropertyValue(entity, name, EntityMode.POJO);
            if (propertyValue == null) {
                continue;
            }
            // find actual property type
            // todo may not be correct see http://www.hibernate.org/hib_docs/v3/api/org/hibernate/type/EntityType.html#getReturnedClass()
            // todo may need to use class name string comparison instead
            Class propertyType;
            Type type = types[i];
            boolean collection = false;
            if (type instanceof CollectionType) {
                CollectionType collectionType = (CollectionType) type;
                propertyType = persister.getFactory().getCollectionPersister(collectionType.getRole()).getElementType().getReturnedClass();
                collection = true;
            } else {
                propertyType = type.getReturnedClass();
            }
            // Mirroring is cascaded for this property?
            if (!compassGps.hasMappingForEntityForMirror(propertyType, cascade)) {
                continue;
            }
            // find dependent unpersisted property value(s)
            ResourceMapping propertyTypeMapping = compassMapping.getMappingByClass(propertyType);
            Mapping[] idMappings = propertyTypeMapping.getIdMappings();
            for (int j = 0, jlen = idMappings.length; j < jlen; j++) {
                ClassPropertyMetaDataMapping idMapping = (ClassPropertyMetaDataMapping) idMappings[j];
                String idPropertyName = idMapping.getPropertyName();
                try {
                    FieldInvoker idGetter = new FieldInvoker(propertyType, idPropertyName).prepare();
                    Object value = new FieldInvoker(entity.getClass(), name).prepare().get(entity);
                    if (collection) {
                        for (Iterator iter = ((Collection) value).iterator(); iter.hasNext();) {
                            Object obj = iter.next();
                            Object id = idGetter.get(obj);
                            if (id == null) {
                                dependencies.add(obj);
                            }
                            dependencies.addAll(getUnpersistedCascades(compassGps, obj, sessionFactory, cascade, visited));
                        }
                    } else {
                        Object id = idGetter.get(value);
                        if (id == null) {
                            dependencies.add(value);
                        }
                        dependencies.addAll(getUnpersistedCascades(compassGps, value, sessionFactory, cascade, visited));
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return dependencies;
    }

    public static void persistPending(CompassSession session, Object entity, Map<Object, Collection> pendingMap, boolean create) {
        for (Iterator iter = pendingMap.keySet().iterator(); iter.hasNext();) {
            Object pending = iter.next();
            Collection dependencies = pendingMap.get(pending);
            if (dependencies.remove(entity)) {
                if (dependencies.isEmpty()) {
                    if (create) {
                        session.create(pending);
                    } else {
                        session.save(pending);
                    }
                    iter.remove();
                }
            }
        }
    }

    private static class RemoveFromPending implements Synchronization {
        private Map<Object, Collection> pendingMap;
        private Object entity;

        public RemoveFromPending(Map<Object, Collection> pendingMap, Object entity) {
            this.pendingMap = pendingMap;
            this.entity = entity;
        }

        private void removePendingDependencies(Object entity, Map<Object, Collection> pendingMap) {
            for (Iterator<Object> iter = pendingMap.keySet().iterator(); iter.hasNext();) {
                Object pending = iter.next();
                if (pending == entity) {
                    iter.remove();
                    break;
                }
                Collection dependencies = pendingMap.get(pending);
                dependencies.remove(entity);
                if (dependencies.isEmpty()) {
                    iter.remove();
                }
            }
        }

        public void afterCompletion(int i) {
            removePendingDependencies(entity, pendingMap);
        }

        public void beforeCompletion() {
        }
    }
}
