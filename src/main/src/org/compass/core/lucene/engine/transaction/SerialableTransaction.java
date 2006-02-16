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

package org.compass.core.lucene.engine.transaction;

import org.compass.core.engine.SearchEngineException;

/**
 * Works the same as the read committed transaction, except that it locks all
 * the index files (per alias) when the transaction begins.
 * <p>
 * Be carefull, it is a very slow transaction.
 * 
 * @author kimchy
 */
public class SerialableTransaction extends ReadCommittedTransaction {

    public void begin() throws SearchEngineException {
        super.begin();
        String[] subIndexes = getIndexManager().getStore().getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            transIndexManager.openTransIndexBySubIndex(subIndexes[i]);
        }
    }
}
