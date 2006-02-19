/*
 * Copyright 2004-2006 the original author or authors.
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
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public final class TransactionManagerLookupFactory {

    private static final Log log = LogFactory.getLog(TransactionManagerLookupFactory.class);

    private TransactionManagerLookupFactory() {
    }

    public static TransactionManagerLookup getTransactionManagerLookup(CompassSettings settings)
            throws TransactionException {
        String tmLookupClass = settings.getSetting(CompassEnvironment.Transaction.MANAGER_LOOKUP);
        if (tmLookupClass == null) {
            log.info("No TransactionManagerLookup configured");
            return null;
        } else {
            log.info("instantiating TransactionManagerLookup [" + tmLookupClass + "]");
            try {
                return (TransactionManagerLookup) ClassUtils.forName(tmLookupClass).newInstance();
            } catch (Exception e) {
                log.error("Could not instantiate TransactionManagerLookup", e);
                throw new TransactionException("Could not instantiate TransactionManagerLookup");
            }
        }
    }
}
