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

import org.compass.core.CompassException;
import org.compass.core.spi.InternalCompassSession;

/**
 * Factory for {@link JTASyncTransaction}s.
 *
 * @author kimchy
 * @see JTASyncTransaction
 */
public class JTASyncTransactionFactory extends AbstractJTATransactionFactory {

    public InternalCompassTransaction doBeginTransaction(InternalCompassSession session) throws CompassException {
        JTASyncTransaction tx = new JTASyncTransaction(getUserTransaction(), commitBeforeCompletion, this);
        tx.begin(session, getTransactionManager());
        return tx;
    }

    protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session) throws CompassException {
        JTASyncTransaction tx = new JTASyncTransaction(getUserTransaction(), commitBeforeCompletion, this);
        tx.join(session);
        return tx;
    }

}
