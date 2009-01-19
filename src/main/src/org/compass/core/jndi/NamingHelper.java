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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

public abstract class NamingHelper {

    private static final Log log = LogFactory.getLog(NamingHelper.class);

    public static InitialContext getInitialContext(CompassSettings settings) throws NamingException {
        Hashtable hash = getJndiProperties(settings);
        try {
            return (hash.size() == 0) ? new InitialContext() : new InitialContext(hash);
        } catch (NamingException e) {
            log.error("Could not obtain initial context with settings [" + hash + "]", e);
            throw e;
        }
    }

    /**
     * Bind val to name in ctx, and make sure that all intermediate contexts
     * exist.
     * 
     * @param ctx
     *            the root context
     * @param name
     *            the name as a string
     * @param val
     *            the object to be bound
     * @throws NamingException
     */
    public static void bind(Context ctx, String name, Object val) throws NamingException {
        try {
            ctx.rebind(name, val);
        } catch (Exception e) {
            Name n = ctx.getNameParser("").parse(name);
            while (n.size() > 1) {
                String ctxName = n.get(0);

                Context subctx = null;
                try {
                    subctx = (Context) ctx.lookup(ctxName);
                } catch (NameNotFoundException nfe) {
                    // don't do nothing
                }

                if (subctx != null) {
                    ctx = subctx;
                } else {
                    ctx = ctx.createSubcontext(ctxName);
                }
                n = n.getSuffix(1);
            }
            ctx.rebind(n, val);
        }
    }

    /**
     * Transform JNDI properties passed in the form
     * <code>compass.jndi. [vaules]</code> to the format accepted by
     * <code>InitialContext</code> by triming the leading "<code>compass.jndi</code>".
     */
    public static Properties getJndiProperties(CompassSettings settings) {
        HashSet specialProps = new HashSet();
        specialProps.add(CompassEnvironment.Jndi.CLASS);
        specialProps.add(CompassEnvironment.Jndi.URL);
        specialProps.add(CompassEnvironment.Jndi.ENABLE);

        Iterator iter = settings.keySet().iterator();
        Properties result = new Properties();
        while (iter.hasNext()) {
            String prop = (String) iter.next();
            if (prop.indexOf(CompassEnvironment.Jndi.PREFIX) > -1 && !specialProps.contains(prop)) {
                result.setProperty(prop.substring(CompassEnvironment.Jndi.PREFIX.length() + 1),
                        settings.getSetting(prop));
            }
        }

        String jndiClass = settings.getSetting(CompassEnvironment.Jndi.CLASS);
        String jndiURL = settings.getSetting(CompassEnvironment.Jndi.URL);
        // we want to be able to just use the defaults,
        // if JNDI environment properties are not supplied
        // so don't put null in anywhere
        if (jndiClass != null)
            result.put(Context.INITIAL_CONTEXT_FACTORY, jndiClass);
        if (jndiURL != null)
            result.put(Context.PROVIDER_URL, jndiURL);

        return result;
    }
}
