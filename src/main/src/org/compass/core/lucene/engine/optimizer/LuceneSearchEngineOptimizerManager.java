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

package org.compass.core.lucene.engine.optimizer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * A {@link org.compass.core.engine.SearchEngineOptimizer} manager that manages the actual
 * {@link LuceneSearchEngineOptimizer}.
 *
 * <p>When configured ({@link #configure(org.compass.core.config.CompassSettings)}, creates the optimizer
 * that will be used based on the {@link org.compass.core.lucene.LuceneEnvironment.Optimizer#TYPE}. The default
 * optimizer used is the {@link org.compass.core.lucene.engine.optimizer.DefaultLuceneSearchEngineOptimizer}.
 *
 * <p>When started, will check the {@link org.compass.core.lucene.LuceneEnvironment.Optimizer#SCHEDULE} flag,
 * and if set to <code>true</code> (the default) will schedule a periodic optimization process. The period
 * is taken from the {@link org.compass.core.lucene.LuceneEnvironment.Optimizer#SCHEDULE_PERIOD} setting, which
 * is set in <b>seconds</b> and defaults to <code>10</code>.
 *
 * @author kimchy
 */
public class LuceneSearchEngineOptimizerManager implements CompassConfigurable, SearchEngineOptimizer {

    private static final Log logger = LogFactory.getLog(LuceneSearchEngineOptimizerManager.class);

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineOptimizer searchEngineOptimizer;

    private ScheduledFuture scheduledFuture;

    private volatile boolean started = false;

    public LuceneSearchEngineOptimizerManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        // build the optimizer and start it
        searchEngineOptimizer = (LuceneSearchEngineOptimizer) settings.getSettingAsInstance(LuceneEnvironment.Optimizer.TYPE, DefaultLuceneSearchEngineOptimizer.class.getName());
        if (logger.isDebugEnabled()) {
            logger.debug("Using optimizer [" + searchEngineOptimizer + "]");
        }
        if (searchEngineOptimizer instanceof SearchEngineFactoryAware) {
            ((SearchEngineFactoryAware) searchEngineOptimizer).setSearchEngineFactory(searchEngineFactory);
        }
    }

    public void start() throws SearchEngineException {
        if (started) {
            return;
        }
        started = true;

        CompassSettings settings = searchEngineFactory.getSettings();
        if (searchEngineOptimizer.canBeScheduled()) {
            boolean scheduledOptimizer = searchEngineFactory.getSettings().getSettingAsBoolean(LuceneEnvironment.Optimizer.SCHEDULE, true);
            if (scheduledOptimizer) {
                long period = (long) (settings.getSettingAsFloat(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, 10) * 1000);
                if (logger.isInfoEnabled()) {
                    logger.info("Starting scheduled optimizer [" + searchEngineOptimizer.getClass() + "] with period [" + period + "ms]");
                }

                ScheduledOptimizeRunnable scheduledOptimizeRunnable = new ScheduledOptimizeRunnable(searchEngineOptimizer);
                scheduledFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(
                        scheduledOptimizeRunnable, period, period, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void stop() throws SearchEngineException {
        if (!started) {
            return;
        }
        started = false;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public boolean isRunning() {
        return scheduledFuture != null;
    }

    public void optimize() throws SearchEngineException {
        searchEngineOptimizer.optimize();
    }

    public void optimize(String subIndex) throws SearchEngineException {
        searchEngineOptimizer.optimize(subIndex);
    }

    public void optimize(int maxNumberOfSegments) throws SearchEngineException {
        searchEngineOptimizer.optimize(maxNumberOfSegments);
    }

    public void optimize(String subIndex, int maxNumberOfSegments) throws SearchEngineException {
        searchEngineOptimizer.optimize(subIndex, maxNumberOfSegments);
    }

    private static class ScheduledOptimizeRunnable implements Runnable {

        private LuceneSearchEngineOptimizer optimizer;

        public ScheduledOptimizeRunnable(LuceneSearchEngineOptimizer optimizer) {
            this.optimizer = optimizer;
        }

        public void run() {
            try {
                optimizer.optimize();
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to optimize", e);
                }
            }
        }
    }
}
