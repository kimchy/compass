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

package org.compass.core.test.concurrency.multi;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.FileHandlerMonitor;

/**
 * Base class for simple concurrency tests for Compass. Starts several threads, each reads and writes data.
 *
 * @author kimchy
 */
public abstract class AbstractMultiLoadTests extends AbstractAnnotationsTestCase {

    private final Log logger = LogFactory.getLog(getClass());

    private long id = 0;

    private final AtomicLong longGenerator = new AtomicLong();

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
        conf.getSettings().setSetting(LuceneEnvironment.Transaction.LOCK_TIMEOUT, "30s");
    }

    /**
     * The number of threads that will be used to execute the test.
     */
    protected abstract int getNumberOfThreads();

    /**
     * The number of Compass instances that will be used. Each thread will pick a compass instace (round robin).
     * If set to 1, all threads will use the same Compass instance.
     */
    protected abstract int getNumberOfCompassInstances();

    /**
     * The number of cycles each thread will run.
     */
    protected abstract long getNumberOfCycles();

    /**
     * The write factor. Every how many cycles, a write will be perfomed.
     */
    protected abstract int getWriteFactor();

    private volatile boolean error = false;

    public void testMultiConcurrentThreads() throws Exception {
        // no need for the original Compass ...
        getCompass().close();

        CompassConfiguration conf = buildConf();
        CompassTemplate[] templates = new CompassTemplate[getNumberOfCompassInstances()];
        for (int i = 0; i < getNumberOfCompassInstances(); i++) {
            Compass compass = conf.buildCompass();
            templates[i] = new CompassTemplate(compass);
        }

        FileHandlerMonitor fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(templates[0].getCompass());
        fileHandlerMonitor.verifyNoHandlers();

        templates[0].getCompass().getSearchEngineIndexManager().deleteIndex();
        templates[0].getCompass().getSearchEngineIndexManager().createIndex();

        Thread[] threads = new Thread[getNumberOfThreads()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new SimpleLoadTesterRunnable(templates[i % getNumberOfCompassInstances()], i, getNumberOfCycles(), getWriteFactor()), "L" + i);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("VERIFYING INDEX USING EXISTING TEMPLATE");
        // now check that everything is in the index
        check(templates[0]);

        for (int i = 0; i < getNumberOfCompassInstances(); i++) {
            templates[i].getCompass().close();
        }

        fileHandlerMonitor.verifyNoHandlers();

        logger.info("VERIFYING INDEX USING NEW COMPASS INSTANCE");
        // now build a new one and check again
        CompassTemplate template = new CompassTemplate(conf.buildCompass());

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(template.getCompass());
        fileHandlerMonitor.verifyNoHandlers();

        check(template);
        template.getCompass().close();

        fileHandlerMonitor.verifyNoHandlers();

        if (error) {
            fail("Multi Concurrent Test Failed, check logs...");
        }
    }

    public class SimpleLoadTesterRunnable implements Runnable {

        private long cycles;

        private long runId;

        private int writeFactor;

        private Long lastIdWritten;

        private CompassTemplate template;

        public SimpleLoadTesterRunnable(CompassTemplate template, long runId, long cycles, int writeFactor) {
            this.cycles = cycles;
            this.runId = runId;
            this.writeFactor = writeFactor;
            this.template = template;
        }

        private String failureStringPrefix(long cycle, Long id) {
            return "FAILURE RUN[" + runId + "] CYCLE[" + cycle + "] ID[" + id + "] ";
        }

        private void check(CompassSession session, long cycle, Long id) {
            A a = session.get(A.class, id);
            if (a == null) {
                error = true;
                logger.error(failureStringPrefix(cycle, id) + " A NULL");
            }

            CompassHits hits = session.find("mdata1:" + id);
            if (0 == hits.length()) {
                error = true;
                logger.error(failureStringPrefix(cycle, id) + " HITS ZERO [" + hits.length() + "]");
            }
        }

        public void run() {
            try {
                long totalTime = System.currentTimeMillis();
                for (long i = 0; i < cycles; i++) {
                    final long cycle = i;

                    if (cycle % writeFactor == 0) {
                        template.execute(new CompassCallbackWithoutResult() {
                            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                                lastIdWritten = longGenerator.incrementAndGet();
                                A a = session.get(A.class, lastIdWritten);
                                if (a != null) {
                                    error = true;
                                    logger.error(failureStringPrefix(cycle, lastIdWritten) + " A NOT NULL [" + a + "]");
                                }

                                CompassHits hits = session.find("mdata1:" + lastIdWritten);
                                if (0 != hits.length()) {
                                    error = true;
                                    logger.error(failureStringPrefix(cycle, lastIdWritten) + " HITS NOT ZERO [" + hits.length() + "]");
                                }

                                a = new A();
                                a.id = lastIdWritten;
                                a.data1 = "" + lastIdWritten;
                                a.indexTime = new Date();
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
                logger.info("FINISHED RUN [" + runId + "] TOOK [" + totalTime + "]");
            } catch (Exception e) {
                error = true;
                logger.error("FAILURE RUN [" + runId + "], THREAD ABORTED...", e);
            }
        }
    }


    private void check(CompassTemplate template) {
        long time = System.currentTimeMillis();
        final long limit = id;
        template.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (long i = 1; i < limit; i++) {
                    A a = session.get(A.class, i);
                    if (a == null) {
                        error = true;
                        logger.error("FAILURE ID [" + i + "] FINAL CHECK NULL");
                    }
                }
            }
        });
        logger.info("FINISHED CHECK [1-" + limit + "] TOOK [" + (System.currentTimeMillis() - time) + "]");
    }
}