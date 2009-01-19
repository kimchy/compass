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

package org.compass.core.jndi;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.config.CompassSettings;

/**
 * Resolves <code>Compass</code> JNDI lookups and deserialization
 */
public class CompassObjectFactory implements ObjectFactory {

    // to stop the class from being unloaded
    private static final CompassObjectFactory INSTANCE;

    private static final Log log;

    static {
        log = LogFactory.getLog(CompassObjectFactory.class);
        INSTANCE = new CompassObjectFactory();
        log.debug("Initializing class CompassObjectFactory. Using static instance [" + INSTANCE + "]");
    }

    private static final Map INSTANCES = new ConcurrentHashMap();

    private static final Map NAMED_INSTANCES = new ConcurrentHashMap();

    private static final NamingListener LISTENER = new NamespaceChangeListener() {
        public void objectAdded(NamingEvent evt) {
            log.debug("Compass was successfully bound to name [" + evt.getNewBinding().getName() + "]");
        }

        public void objectRemoved(NamingEvent evt) {
            String name = evt.getOldBinding().getName();
            if (log.isInfoEnabled()) {
                log.info("Compass was unbound from name [" + name + "]");
            }
            Object instance = NAMED_INSTANCES.remove(name);
            Iterator iter = INSTANCES.values().iterator();
            while (iter.hasNext()) {
                if (iter.next() == instance)
                    iter.remove();
            }
        }

        public void objectRenamed(NamingEvent evt) {
            String name = evt.getOldBinding().getName();
            if (log.isInfoEnabled()) {
                log.info("Compass was renamed from name [" + name + "]");
            }
            NAMED_INSTANCES.put(evt.getNewBinding().getName(), NAMED_INSTANCES.remove(name));
        }

        public void namingExceptionThrown(NamingExceptionEvent evt) {
            log.warn("Naming exception occurred accessing compass: " + evt.getException());
        }
    };

    public Object getObjectInstance(Object reference, Name name, Context ctx, Hashtable env) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("JNDI lookup for [" + name + "]");
        }
        String uid = (String) ((Reference) reference).get(0).getContent();
        return getInstance(uid);
    }

    public static void addInstance(String uid, String name, Compass instance, CompassSettings settings) {

        if (log.isDebugEnabled()) {
            log.debug("Registering compass [" + uid + "] with [" + ((name == null) ? "unnamed" : name) + ']');
        }
        INSTANCES.put(uid, instance);
        if (name != null)
            NAMED_INSTANCES.put(name, instance);

        // must add to JNDI _after_ adding to HashMaps, because some JNDI
        // servers use serialization
        if (name == null) {
            log.info("Not binding compass to JNDI, no JNDI name configured");
        } else {

            if (log.isInfoEnabled()) {
                log.info("Binding compass to JNDI under [" + name + "]");
            }

            try {
                Context ctx = NamingHelper.getInitialContext(settings);
                NamingHelper.bind(ctx, name, instance);
                ((EventContext) ctx).addNamingListener(name, EventContext.OBJECT_SCOPE, LISTENER);
            } catch (InvalidNameException ine) {
                log.error("Invalid JNDI name [" + name + "]", ine);
            } catch (NamingException ne) {
                log.warn("Could not bind compass to JNDI", ne);
            } catch (ClassCastException cce) {
                log.warn("InitialContext did not implement EventContext");
            }

        }

    }

    public static void removeInstance(String uid, String name, CompassSettings settings) {
        // TODO: theoretically non-threadsafe...

        if (name != null) {
            if (log.isInfoEnabled()) {
                log.info("Unbinding compass from JNDI name [" + name + "]");
            }

            try {
                Context ctx = NamingHelper.getInitialContext(settings);
                ctx.unbind(name);
            } catch (InvalidNameException ine) {
                log.error("Invalid JNDI name [" + name + "]", ine);
            } catch (NamingException ne) {
                log.warn("Could not unbind compass from JNDI", ne);
            }

            NAMED_INSTANCES.remove(name);

        }

        INSTANCES.remove(uid);

    }

    public static Object getNamedInstance(String name) {
        if (log.isDebugEnabled()) {
            log.debug("lookup with name [" + name + "]");
        }
        Object result = NAMED_INSTANCES.get(name);
        if (result == null) {
            log.warn("Not found [" + name + "]");
            log.debug(NAMED_INSTANCES);
        }
        return result;
    }

    public static Object getInstance(String uid) {
        if (log.isDebugEnabled()) {
            log.debug("JNDI lookup for uid [" + uid + "]");
        }
        Object result = INSTANCES.get(uid);
        if (result == null) {
            log.warn("Not found [" + uid + "]");
            log.debug(INSTANCES);
        }
        return result;
    }

}
