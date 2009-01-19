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

package org.compass.spring.device.hibernate.dep;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.dep.Hibernate3GpsDevice;
import org.hibernate.SessionFactory;

/**
 * A special <code>Hibernate3GpsDevice</code> that handled cases when spring
 * proxies the <code>SessionFactory</code> (like when
 * <code>exposeTransactionAwareSessionFactory</code> is set to
 * <code>true</code>, which is the default from spring 1.2.X).
 * <p>
 * Use this hibernate gps device instead of
 * <code>org.compass.gps.device.hibernate.dep.Hibernate3GpsDevice</code> if you
 * are using Hibernate 3 and Spring.
 * 
 * @author kimchy
 */
public class SpringHibernate3GpsDevice extends Hibernate3GpsDevice {

    public static SessionFactory getNativeSessionFactory(SessionFactory sessionFactory) {
        if (Proxy.isProxyClass(sessionFactory.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(sessionFactory);
            try {
                Field target = invocationHandler.getClass().getDeclaredField("target");
                target.setAccessible(true);
                sessionFactory = (SessionFactory) target.get(invocationHandler);
            } catch (Exception e) {
                throw new HibernateGpsDeviceException("Failed to fetch actual session factory, " +
                        "sessionFactory[" + sessionFactory.getClass().getName() + "], " +
                        "invocationHandler[" + invocationHandler.getClass().getName() + "]", e);
            }
        }
        return sessionFactory;
    }

    /**
     * Returns the actual <code>SessionFactory</code> in case it is proxied by
     * spring.
     */
    protected SessionFactory doGetActualSessionFactory() {
        return getNativeSessionFactory(super.doGetActualSessionFactory());
    }
}
