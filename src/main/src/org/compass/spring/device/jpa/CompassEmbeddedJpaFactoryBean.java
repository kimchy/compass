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

package org.compass.spring.device.jpa;

import javax.persistence.EntityManagerFactory;

import org.compass.gps.device.jpa.embedded.GenericJpaHelper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Spring factory bean that returns a {@link org.compass.core.Compass} instance
 * that is embedded within the provided JPA {@link javax.persistence.EntityManagerFactory}.
 *
 * @author kimchy
 * @see org.compass.gps.device.jpa.embedded.GenericJpaHelper
 */
public class CompassEmbeddedJpaFactoryBean implements FactoryBean, InitializingBean {

    private EntityManagerFactory entityManagerFactory;

    private GenericJpaHelper jpaHelper;

    public CompassEmbeddedJpaFactoryBean(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void afterPropertiesSet() throws Exception {
        this.jpaHelper = new GenericJpaHelper(entityManagerFactory);
    }

    public Object getObject() throws Exception {
        return jpaHelper.getCompass();
    }

    public Class getObjectType() {
        return jpaHelper.getCompass().getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
