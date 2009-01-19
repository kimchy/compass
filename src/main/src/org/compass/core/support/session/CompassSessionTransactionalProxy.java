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

package org.compass.core.support.session;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompassSession;

/**
 * InvocationHandler for {@link org.compass.core.CompassSession}. Used
 * <b>within already transactional context</b> in order to simplify the usage
 * of CompassSession. With this wrapper, there is no need to call <code>openSession</code>,
 * or <code>close()</code> on the session. There is no need to use Compass transaction
 * API as well.
 *
 * @author kimchy
 */
public class CompassSessionTransactionalProxy implements InvocationHandler, Serializable {

    private Compass compass;

    /**
     * Constrcuts a new invocation handler based on the session. Allows for
     * proxing {@link org.compass.core.CompassSession} for simpler API usage
     * <b>within a transactional context</b>.
     *
     * @param compass The Compass instance to use for session creation
     */
    public CompassSessionTransactionalProxy(Compass compass) {
        this.compass = compass;
    }

    /**
     * Creates a new proxied {@link org.compass.core.CompassSession} that can
     * be used within an already running transcational context without worrying
     * about session management API or transcation management API.
     *
     * @param compass The compass instance used to create the session
     * @return A proxied session for simpler session usage within an already running transactional context
     */
    public static CompassSession newProxy(Compass compass) {
        return (CompassSession) Proxy.newProxyInstance(
                InternalCompassSession.class.getClassLoader(),
                new Class[]{InternalCompassSession.class},
                new CompassSessionTransactionalProxy(compass));
    }

    /**
     * Since Compass already does most of the session management, this wrapper around
     * {@link org.compass.core.CompassSession} invocation simply uses {@link org.compass.core.Compass#openSession()}
     * and invoke the appropiate method on the session returned. Compass will make sure that this is the correct
     * session within the context.
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("equals")) {
            // Only consider equal when proxies are identical.
            return proxy == args[0];
        } else if (method.getName().equals("hashCode")) {
            // Use hashCode of Compass proxy.
            return hashCode();
        }
        InternalCompassSession session = (InternalCompassSession) compass.openSession();
        try {
            return method.invoke(session, args);
        }
        catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
}
