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

package org.compass.core.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.transaction.manager.Glassfish;
import org.compass.core.transaction.manager.JBoss;
import org.compass.core.transaction.manager.JOTM;
import org.compass.core.transaction.manager.JOnAS;
import org.compass.core.transaction.manager.JRun4;
import org.compass.core.transaction.manager.OC4J;
import org.compass.core.transaction.manager.Orion;
import org.compass.core.transaction.manager.Resin;
import org.compass.core.transaction.manager.WebSphere;
import org.compass.core.transaction.manager.Weblogic;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public final class TransactionManagerLookupFactory {

    private static final Log log = LogFactory.getLog(TransactionManagerLookupFactory.class);

    private static Class[] autoDetectOrder = {WebSphere.class, Weblogic.class, JOnAS.class, JOTM.class, JBoss.class,
            Glassfish.class, Orion.class, Resin.class, OC4J.class, JRun4.class};

    private TransactionManagerLookupFactory() {
    }

    public static TransactionManagerLookup getTransactionManagerLookup(CompassSettings settings)
            throws TransactionException {
        String tmLookupClass = settings.getSetting(CompassEnvironment.Transaction.MANAGER_LOOKUP);
        if (tmLookupClass == null) {
            // try and auto detect the transaction manager
            log.info("JTA Transaction Manager Lookup setting not found, auto detecting....");
            for (Class anAutoDetectOrder : autoDetectOrder) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying [" + anAutoDetectOrder.getName() + "]");
                }
                TransactionManagerLookup tmLookup = detect(anAutoDetectOrder, settings);
                if (tmLookup != null) {
                    log.info("Detected JTA Transaction Manager [" + anAutoDetectOrder.getName() + "]");
                    return tmLookup;
                }
            }
            return null;
        } else {
            log.debug("Instansiating TransactionManagerLookup [" + tmLookupClass + "]");
            try {
                return (TransactionManagerLookup) ClassUtils.forName(tmLookupClass, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new TransactionException("Could not instantiate TransactionManagerLookup", e);
            }
        }
    }

    private static TransactionManagerLookup detect(Class tmClass, CompassSettings settings) {
        try {
            TransactionManagerLookup tmLookup = (TransactionManagerLookup) tmClass.newInstance();
            if (tmLookup.getTransactionManager(settings) != null) {
                return tmLookup;
            }
        } catch (Exception e) {
            // do nothing here
        }
        return null;
    }
}
