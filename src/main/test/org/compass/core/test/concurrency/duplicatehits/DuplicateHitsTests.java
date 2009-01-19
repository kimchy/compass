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

package org.compass.core.test.concurrency.duplicatehits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;


public class DuplicateHitsTests extends TestCase {

    private static final String ALIAS_A_NAME_UNIQUE_NAME = "+alias:a +name:";

    private static final String UNIQUE_ID = "uniqueid";

    private static final String UNIQUE_NAME = "uniqueName";

    private CompassTemplate compassTemplate;

    private Compass compass;

    protected void setUp() throws Exception {
        super.setUp();
        CompassConfiguration compassConfiguration = new CompassConfiguration();
        compassConfiguration.addClass(A.class);
        compassConfiguration.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        compassConfiguration.setSetting(CompassEnvironment.Transaction.FACTORY, "org.compass.core.transaction.LocalTransactionFactory");

        compass = compassConfiguration.buildCompass();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();
        compassTemplate = new CompassTemplate(compass);
    }

    protected void tearDown() throws Exception {
        compass.close();
        compass.getSearchEngineIndexManager().deleteIndex();
    }

    public void testSearchingObjectsAtTheSameTimeAsChangingThemWontCauseTheSameObjectToBeReturnedTwiceInCompassHits() throws Exception {
        A toBeSearched = new A();
        toBeSearched.setId(UNIQUE_ID);
        toBeSearched.setName(UNIQUE_NAME);
        toBeSearched.setDescription("description");

        compassTemplate.save(toBeSearched);
        CompassDetachedHits hits = compassTemplate.findWithDetach(ALIAS_A_NAME_UNIQUE_NAME + UNIQUE_NAME);
        assertEquals("before", 1, hits.length());
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        final long mutateDelay = 1;
        final long searchDelay = 1;
        Runnable mutate = new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    mutate("desc" + i, mutateDelay);
                }
            }
        };
        Runnable search = new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    search(searchDelay);
                }
            }
        };
        List results = new ArrayList();
        results.add(executorService.submit(search));
        for (int i = 1; i <= 1; i++) {
            results.add(executorService.submit(mutate));
        }
        for (Iterator it = results.iterator(); it.hasNext();) {
            Future future = (Future) it.next();
            future.get();
        }
        executorService.shutdown();
    }

    private void mutate(String description, long delay) {

        A reloaded = (A) compassTemplate.load("a", UNIQUE_ID);
        reloaded.setDescription(description);
        compassTemplate.save(reloaded);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }

    }

    private void search(long delay) {
        CompassDetachedHits hitsAfter = compassTemplate.findWithDetach(ALIAS_A_NAME_UNIQUE_NAME + UNIQUE_NAME);
        if (hitsAfter.length() > 1) {
            Set cps = new HashSet();
            ArrayList results = new ArrayList();
            for (int i = 0; i < hitsAfter.length(); i++) {

                A toBeSearched = (A) hitsAfter.data(i);
                cps.add(toBeSearched.getId());
                results.add(toBeSearched);
            }
            assertTrue("the ids for the returned objects should be unique" + results, cps.size() == hitsAfter.length());

        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }

}


