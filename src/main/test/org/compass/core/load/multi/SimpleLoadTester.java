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

package org.compass.core.load.multi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import org.compass.core.*;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class SimpleLoadTester {

    private static CompassTemplate template;

    private static Long id = new Long(0);

    private static final Object idLock = new Object();

    private static Long createNextId() {
        synchronized (idLock) {
            id = new Long(id.longValue() + 1);
            return id;
        }
    }

    public static class SimpleLoadTesterRunnable implements Runnable {

        private long cycles;

        private long runId;

        private int writeFactor;

        private Long lastIdWritten;

        public SimpleLoadTesterRunnable(long runId, long cycles, int writeFactor) {
            this.cycles = cycles;
            this.runId = runId;
            this.writeFactor = writeFactor;
        }

        private String failureStringPrefix(long cycle, Long id) {
            return "FAILURE RUN[" + runId + "] CYCLE[" + cycle + "] ID[" + id + "] ";
        }

        private void check(CompassSession session, long cycle, Long id) {
            A a = (A) session.get(A.class, id);
            if (a == null) {
                System.err.println(failureStringPrefix(cycle, id) + " A NULL");
            }

            CompassHits hits = session.find("mdata1:" + id);
            if (0 == hits.length()) {
                System.err.println(failureStringPrefix(cycle, id) + " HITS ZERO [" + hits.length() + "]");
            }
        }

        public void run() {
            long totalTime = System.currentTimeMillis();
            for (long i = 0; i < cycles; i++) {
                final long cycle = i;

                if (cycle % writeFactor == 0) {
                    template.execute(new CompassCallbackWithoutResult() {
                        protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                            lastIdWritten = createNextId();
                            A a = (A) session.get(A.class, lastIdWritten);
                            if (a != null) {
                                System.err.println(failureStringPrefix(cycle, lastIdWritten) + " A NOT NULL [" + a + "]");
                            }

                            CompassHits hits = session.find("mdata1:" + lastIdWritten);
                            if (0 != hits.length()) {
                                System.err.println(failureStringPrefix(cycle, lastIdWritten) + " HITS NOT ZERO [" + hits.length() + "]");
                            }

                            a = new A();
                            a.setId(lastIdWritten);
                            a.setData1("" + lastIdWritten);
                            a.setIndexTime(new Date());
                            session.save(a);

                            check(session, cycle, lastIdWritten);
                        }
                    });
                } else {
                    template.execute(new CompassCallbackWithoutResult() {
                        protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                            check(session, cycle, lastIdWritten);
                        }
                    });
                }

            }
            totalTime = System.currentTimeMillis() - totalTime;
            System.out.println("FINISHED RUN [" + runId + "] TOOK [" + totalTime + "]");
        }
    }


    public static void main(String[] args) throws Exception {

        int numberOfRuns = 5;
        long numberOfCycles = 200;
        int writeFactor = 10;

        CompassConfiguration conf = new CompassConfiguration();
        conf.configure("org/compass/core/load/multi/compass.cfg.xml");
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            conf.getSettings().addSettings(testProps);
        }
        conf.addClass(A.class);

        Compass compass = conf.buildCompass();
//        compass.getSearchEngineOptimizer().stop();
//        compass.getSearchEngineIndexManager().stop();
        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().createIndex();

        template = new CompassTemplate(compass);

        Thread[] threads = new Thread[numberOfRuns];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new SimpleLoadTesterRunnable(i, numberOfCycles, writeFactor), "L" + i);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // now check that everything is in the index
        long time = System.currentTimeMillis();
        long limit = id.longValue();
        for (long i = 1; i < limit; i++) {
            final Long id = new Long(i);
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    A a = (A) session.get(A.class, id);
                    if (a == null) {
                        System.err.println("FAILURE ID [" + id + "] FINAL CHECK NULL");
                    }
                }
            });
        }
        System.out.println("FINISHED CHECK [1-" + limit + "] TOOK [" + (System.currentTimeMillis() - time) + "]");

        compass.close();
    }
}
