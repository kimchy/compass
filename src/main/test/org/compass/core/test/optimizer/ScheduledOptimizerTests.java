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

package org.compass.core.test.optimizer;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.impl.DefaultCompass;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.optimizer.AbstractLuceneSearchEngineOptimizer;
import org.compass.core.lucene.engine.optimizer.ScheduledLuceneSearchEngineOptimizer;

/**
 * @author kimchy
 */
public class ScheduledOptimizerTests extends AbstractOptimizerTests {

    public static class MockOptimizer extends AbstractLuceneSearchEngineOptimizer {

        private boolean needOptimizingCalled;

        private boolean optimizeCalled;

        private boolean needOptimizing;

        protected boolean isOptimizeOnlyIfIndexChanged() {
            return false;
        }

        public boolean needOptimizing(String subIndex, LuceneSubIndexInfo indexInfo) {
            needOptimizingCalled = true;
            return needOptimizing;
        }

        protected void doOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException {
            optimizeCalled = true;
            needOptimizing = false;
        }

        public void clear() {
            needOptimizingCalled = false;
            optimizeCalled = false;
            needOptimizing = false;
        }

        public boolean isNeedOptimizingCalled() {
            return needOptimizingCalled;
        }

        public boolean isOptimizeCalled() {
            return optimizeCalled;
        }

        public boolean isNeedOptimizing() {
            return needOptimizing;
        }

        public void setNeedOptimizing(boolean needOptimizing) {
            this.needOptimizing = needOptimizing;
        }

        public boolean canBeScheduled() {
            return true;
        }
    }

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setSetting(LuceneEnvironment.Optimizer.TYPE, MockOptimizer.class.getName());
        settings.setBooleanSetting(LuceneEnvironment.Optimizer.SCHEDULE, true);
        settings.setSetting(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, "0.1");
        settings.setSetting(LuceneEnvironment.Optimizer.Aggressive.MERGE_FACTOR, "3");
    }

    public void testScheduledOptimizer() {
        MockOptimizer optimizer = (MockOptimizer) ((DefaultCompass.TransactionalSearchEngineOptimizer) ((ScheduledLuceneSearchEngineOptimizer) getCompass()
                .getSearchEngineOptimizer()).getWrappedOptimizer()).getWrappedOptimizer();
        assertTrue(getCompass().getSearchEngineOptimizer().isRunning());
        optimizer.clear();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        assertTrue(optimizer.isNeedOptimizingCalled());
        assertFalse(optimizer.isOptimizeCalled());

        optimizer.clear();
        optimizer.setNeedOptimizing(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        assertTrue(optimizer.isNeedOptimizingCalled());
        assertTrue(optimizer.isOptimizeCalled());

        // stop the optimizer, make sure that it won't schedule any more optimizations
        getCompass().getSearchEngineOptimizer().stop();
        assertFalse(getCompass().getSearchEngineOptimizer().isRunning());
        optimizer.clear();
        optimizer.setNeedOptimizing(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        assertFalse(optimizer.isNeedOptimizingCalled());
        assertFalse(optimizer.isOptimizeCalled());
    }
}
