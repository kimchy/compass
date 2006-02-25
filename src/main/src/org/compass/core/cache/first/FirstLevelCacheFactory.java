package org.compass.core.cache.first;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class FirstLevelCacheFactory implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(FirstLevelCacheFactory.class);

    private Class firstLevelCacheClass;

    public void configure(CompassSettings settings) throws CompassException {
        // create the first level cache based on the transaction isolation level
        String transIsolationSetting = settings.getSetting(CompassEnvironment.Transaction.ISOLATION, null);

        if (transIsolationSetting != null
                && transIsolationSetting.equalsIgnoreCase(CompassEnvironment.Transaction.ISOLATION_BATCH_INSERT)) {
            firstLevelCacheClass = NullFirstLevelCache.class;
        } else {
            String firstLevelCacheSetting = settings.getSetting(CompassEnvironment.Cache.FirstLevel.TYPE,
                    DefaultFirstLevelCache.class.getName());
            try {
                firstLevelCacheClass = ClassUtils.forName(firstLevelCacheSetting);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Failed to find class name [" + firstLevelCacheSetting
                        + "] for first level cache", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Using first level cache [" + firstLevelCacheClass.getName() + "]");
        }
    }

    public FirstLevelCache createFirstLevelCache() throws CompassException {
        try {
            return (FirstLevelCache) firstLevelCacheClass.newInstance();
        } catch (Exception e) {
            throw new CompassException("Failed to create first level cache", e);
        }
    }
}
