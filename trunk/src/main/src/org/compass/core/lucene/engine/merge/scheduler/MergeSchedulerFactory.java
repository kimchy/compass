package org.compass.core.lucene.engine.merge.scheduler;

import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.SerialMergeScheduler;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.util.ClassUtils;

/**
 * A {@link org.apache.lucene.index.MergeScheduler} factory using {@link MergeSchedulerProvider}
 * to create one.
 *
 * @author kimchy
 */
public class MergeSchedulerFactory {

    public static MergeScheduler create(LuceneSearchEngineIndexManager indexManager, CompassSettings settings) throws SearchEngineException {
        if (!indexManager.supportsConcurrentOperations()) {
            return new SerialMergeScheduler();
        }
        String type = settings.getSetting(LuceneEnvironment.MergeScheduler.TYPE, LuceneEnvironment.MergeScheduler.Executor.NAME);
        MergeSchedulerProvider provider;
        if (type.equals(LuceneEnvironment.MergeScheduler.Executor.NAME)) {
            provider = new ExecutorMergeSchedulerProvider();
        } else if (type.equals(LuceneEnvironment.MergeScheduler.Concurrent.NAME)) {
            provider = new ConcurrentMergeSchedulerProvider();
        } else if (type.equals(LuceneEnvironment.MergeScheduler.Serial.NAME)) {
            provider = new SerialMergeSchedulerProvider();
        } else {
            try {
                provider = (MergeSchedulerProvider) ClassUtils.forName(type, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new SearchEngineException("Failed to create merge scheduler provider [" + type + "]", e);
            }
        }
        return provider.create(indexManager, settings);
    }
}
