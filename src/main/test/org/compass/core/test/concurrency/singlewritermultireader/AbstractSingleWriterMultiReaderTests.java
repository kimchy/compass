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

package org.compass.core.test.concurrency.singlewritermultireader;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public abstract class AbstractSingleWriterMultiReaderTests extends AbstractTestCase {

    private Log logger = LogFactory.getLog(getClass());

    private AtomicLong lastId = new AtomicLong();

    private volatile boolean writerDone = false;

    private volatile boolean error = false;

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    protected abstract long getNumberOfObjects();

    protected abstract int getNumberOfReaders();

    public void testSingleWriterMultiReader() {
        Thread writerThread = new Thread(new Runnable() {
            public void run() {
                CompassTemplate template = new CompassTemplate(getCompass());
                try {
                    for (long count = 0; count < getNumberOfObjects(); count++) {
                        A a = new A();
                        a.id = count;
                        a.data1 = "" + count;
                        template.save(a);
                        lastId.incrementAndGet();
                    }
                } catch (Exception e) {
                    error = true;
                    logger.error("WRITER THREAD FAILED", e);
                }
                writerDone = true;
            }
        });
        writerThread.setName("WRITER");

        Thread[] readerThreads = new Thread[getNumberOfReaders()];
        for (int i = 0; i < readerThreads.length; i++) {
            readerThreads[i] = new Thread(new Runnable() {
                public void run() {
                    CompassTemplate template = new CompassTemplate(getCompass());
                    while (!writerDone) {
                        try {
                            for (long i = 0; i < lastId.get(); i++) {
                                A a = template.get(A.class, i);
                                if (a == null) {
                                    error = true;
                                    logger.error("FAILED TO READ ID [" + i + "]");
                                }
                            }
                        } catch (Exception e) {
                            error = true;
                            logger.error("READER THREAD FAILED", e);
                        }
                    }
                }
            });
            readerThreads[i].setName("READER[" + i + "]");
        }
        writerThread.start();
        for (Thread readerThread : readerThreads) {
            readerThread.start();
        }

        try {
            writerThread.join();
        } catch (InterruptedException e) {
            logger.error("Failed to join on writer thread, interrupted", e);
        }
        for (Thread readerThread : readerThreads) {
            try {
                readerThread.join();
            } catch (InterruptedException e) {
                logger.error("Failed to join on reader thread, interrupted", e);
            }
        }

        if (error) {
            fail("Single writer multi writer test failed, see logs...");
        }
    }
}
