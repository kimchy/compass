package org.compass.spring.device.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.NativeHibernateExtractor;
import org.hibernate.SessionFactory;

/**
 * A Spring hibernate native SessionFactory extractor.
 *
 * @author kimchy
 */
public class SpringNativeHibernateExtractor implements NativeHibernateExtractor {

    public SessionFactory extractNative(SessionFactory sessionFactory) throws HibernateGpsDeviceException {
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
}
