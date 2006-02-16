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

package org.compass.core.lucene.engine.optimizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * @author kimchy
 */
public class LuceneSearchEngineScheduledOptimizer implements LuceneSearchEngineOptimizer {

    final static private Log log = LogFactory.getLog(LuceneSearchEngineScheduledOptimizer.class);

    private LuceneSearchEngineOptimizer optimizer;

    private boolean daemon;

    private long period;

    private boolean fixedRate;

    private ScheduledOptimizeThread thread;

    private SearchEngineFactory searchEngineFactory;

    public LuceneSearchEngineScheduledOptimizer(LuceneSearchEngineOptimizer optimizer, SearchEngineFactory searchEngineFactory) {
        this.optimizer = optimizer;
        this.searchEngineFactory = searchEngineFactory;
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return this.optimizer.getSearchEngineFactory();
    }

    public void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory) {
        this.optimizer.setSearchEngineFactory(searchEngineFactory);
    }

    public boolean needOptimization() throws SearchEngineException {
        return this.optimizer.needOptimization();
    }

    public void optimize() throws SearchEngineException {
        this.optimizer.optimize();
    }

    public boolean needOptimizing(String subIndex) throws SearchEngineException {
        return this.optimizer.needOptimizing(subIndex);
    }

    public boolean needOptimizing(String subIndex, LuceneSubIndexInfo segmentInfos) throws SearchEngineException {
        return this.optimizer.needOptimizing(subIndex, segmentInfos);
    }

    public void optimize(String subIndex, LuceneSubIndexInfo segmentInfos) throws SearchEngineException {
        this.optimizer.optimize(subIndex, segmentInfos);
    }

    public synchronized void start() throws SearchEngineException {
        if (isRunning()) {
            throw new IllegalStateException("Optimizer is already running");
        }

        CompassSettings settings = getSearchEngineFactory().getSettings();
        daemon = settings.getSettingAsBoolean(LuceneEnvironment.Optimizer.SCHEDULE_DEAMON, true);
        period = (long) (settings.getSettingAsFloat(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, 10) * 1000);
        fixedRate = settings.getSettingAsBoolean(LuceneEnvironment.Optimizer.SCHEDULE_FIXEDRATE, false);
        if (log.isInfoEnabled()) {
            log.info("Starting scheduled optimizer [" + optimizer.getClass() + "] with period [" + period
                    + "ms] fixed rate [" + fixedRate + "] daemon [" + daemon + "]");
        }

        this.optimizer.start();
        thread = new ScheduledOptimizeThread(this.optimizer, period, searchEngineFactory.getIndexManager());
        thread.setDaemon(daemon);
        thread.setName("Compass Optimizer");
        thread.start();
    }

    public synchronized void stop() throws SearchEngineException {
        if (!isRunning()) {
            throw new IllegalStateException("Optimizer is not running");
        }
        if (log.isInfoEnabled()) {
            log.info("Stopping scheduled optimizer [" + optimizer.getClass() + "]");
        }
        thread.cancel();
        thread = null;
        this.optimizer.stop();
    }

    public boolean isRunning() {
        return this.optimizer.isRunning();
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public boolean isFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(boolean fixedRate) {
        this.fixedRate = fixedRate;
    }

    public LuceneSearchEngineOptimizer getWrappedOptimizer() {
        return this.optimizer;
    }

    public boolean canBeScheduled() {
        return false;
    }


    private static class ScheduledOptimizeThread extends Thread {

        private OptimizerTemplate optimizerTemplate;

        private long period;

        private boolean canceled;

        public ScheduledOptimizeThread(LuceneSearchEngineOptimizer optimizer, long period, SearchEngineIndexManager indexManager) {
            this.optimizerTemplate = new OptimizerTemplate(optimizer, indexManager);
            this.period = period;
        }

        public void run() {
            while (!Thread.interrupted() || !canceled) {
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    break;
                }
                try {
                    optimizerTemplate.optimize();
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed to optimize", e);
                    }
                }
            }
        }

        public void cancel() {
            optimizerTemplate.cancel();
            this.canceled = true;
            this.interrupt();
        }
    }
}
