package org.compass.core.test.cache;

import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class NoneAsyncInvalidationCacheTests extends AbstractCacheTests {

    protected void addSettings(CompassSettings settings) {
        super.addSettings(settings);
        settings.setBooleanSetting(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, false);
    }
}