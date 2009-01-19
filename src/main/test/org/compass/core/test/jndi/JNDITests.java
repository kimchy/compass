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

package org.compass.core.test.jndi;

import java.io.File;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class JNDITests extends TestCase {

    public void testJNDI() throws Exception {
        File testJndiDir = new File("target/jndi");
        testJndiDir.mkdirs();
        String jndiPath = testJndiDir.toURL().toExternalForm();
        CompassConfiguration conf = new CompassConfiguration().configure("/org/compass/core/test/jndi/compass.cfg.xml");
        Compass sessionFactory = conf.buildCompass();

        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        env.put(Context.PROVIDER_URL, jndiPath);
        Context initCtx = new InitialContext(env);
        Compass jndiFactory = (Compass) initCtx.lookup("compass/CompassFactory");
        assertNotNull(jndiFactory);

        sessionFactory.close();
    }

}
