package org.compass.gps.device.hibernate.simple;

import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public class LocalCacheSimpleHibernateGpsDeviceTests extends ScrollableSimpleHibernateGpsDeviceTests {

    protected void setUpCoreCompass(CompassConfiguration conf) {
        super.setUpCoreCompass(conf);
        conf.getSettings().setGroupSettings(LuceneEnvironment.LocalCache.PREFIX, LuceneEnvironment.LocalCache.DEFAULT_NAME,
                new String[]{LuceneEnvironment.LocalCache.CONNECTION}, new String[]{"target/test-index-cache"});
    }

}
