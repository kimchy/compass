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

package org.compass.spring.device.hibernate;

import org.compass.core.Compass;
import org.compass.gps.device.hibernate.embedded.HibernateHelper;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Spring factory bean that returns a {@link org.compass.core.Compass} instance
 * that is embedded within the provided Hibernate {@link org.hibernate.SessionFactory}.
 *
 * @author kimchy
 * @see org.compass.gps.device.hibernate.embedded.HibernateHelper
 */
public class CompassEmbeddedHibernateFactoryBean implements FactoryBean, InitializingBean {

    private SessionFactory sessionFactory;

    public CompassEmbeddedHibernateFactoryBean(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void afterPropertiesSet() throws Exception {
    }

    public Object getObject() throws Exception {
        return HibernateHelper.getCompass(sessionFactory);
    }

    public Class getObjectType() {
        return Compass.class;
    }

    public boolean isSingleton() {
        return true;
    }
}