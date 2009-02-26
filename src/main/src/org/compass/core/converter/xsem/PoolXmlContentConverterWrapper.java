package org.compass.core.converter.xsem;

import java.io.Reader;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;

/**
 * An {@link XmlContentConverter} implementation that wraps the actual {@link XmlContentConverter}
 * configured (based on the settings) and creates and configures a pool of {@link XmlContentConverter}s
 * for both {@link #toXml(org.compass.core.xml.XmlObject)} and {@link #fromXml(String, java.io.Reader)}.
 * <p/>
 * The pool has a maximum capacity, to limit overhead. If all instances in the
 * pool are in use and another is required, it shall block until one becomes
 * available.
 *
 * @author kimchy
 */
public class PoolXmlContentConverterWrapper implements XmlContentConverterWrapper, CompassConfigurable {

    private CompassSettings settings;

    private int initialPoolSize;

    private int maxPoolSize;

    private transient XmlContentConverter[] pool;

    private int nextAvailable = 0;

    private final Object mutex = new Object();

    /**
     * Configures the pool used from {@link CompassEnvironment.Xsem.XmlContent#MIN_POOL_SIZE} and
     * {@link CompassEnvironment.Xsem.XmlContent#MAX_POOL_SIZE}.
     */
    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        this.initialPoolSize = settings.getGloablSettings().getSettingAsInt(CompassEnvironment.Xsem.XmlContent.MIN_POOL_SIZE, 10);
        this.maxPoolSize = settings.getGloablSettings().getSettingAsInt(CompassEnvironment.Xsem.XmlContent.MAX_POOL_SIZE, 30);
        // warm up the pool
        XmlContentConverter converter = fetchFromPool();
        putInPool(converter);
    }

    /**
     * Converts the {@link XmlObject} into raw xml by using the pool of
     * {@link XmlContentConverter}s implementation.
     *
     * @see XmlContentConverter#toXml(org.compass.core.xml.XmlObject)
     */
    public String toXml(XmlObject xmlObject) throws ConversionException {
        XmlContentConverter converter = fetchFromPool();
        try {
            return converter.toXml(xmlObject);
        } finally {
            putInPool(converter);
        }
    }

    /**
     * Converts a raw xml and an alias into an {@link AliasedXmlObject} by using the pool of
     * {@link XmlContentConverter}s implementation.
     *
     * @see XmlContentConverter#fromXml(String, java.io.Reader)
     */
    public AliasedXmlObject fromXml(String alias, Reader xml) throws ConversionException {
        XmlContentConverter converter = fetchFromPool();
        try {
            return converter.fromXml(alias, xml);
        } finally {
            putInPool(converter);
        }
    }

    public XmlContentConverter createContentConverter() {
        return XmlContentConverterUtils.createXmlContentConverter(settings);
    }

    private XmlContentConverter fetchFromPool() {
        XmlContentConverter result;
        synchronized (mutex) {
            if (pool == null) {
                nextAvailable = -1;
                pool = new XmlContentConverter[maxPoolSize];
                for (int i = 0; i < initialPoolSize; i++) {
                    putInPool(createContentConverter());
                }
            }
            while (nextAvailable < 0) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new ConversionException("Interrupted whilst waiting for a free item in the pool", e);
                }
            }
            result = pool[nextAvailable];
            nextAvailable--;
        }
        if (result == null) {
            result = createContentConverter();
            putInPool(result);
        }
        return result;
    }

    private void putInPool(XmlContentConverter converter) {
        synchronized (mutex) {
            nextAvailable++;
            pool[nextAvailable] = converter;
            mutex.notify();
        }
    }
}
