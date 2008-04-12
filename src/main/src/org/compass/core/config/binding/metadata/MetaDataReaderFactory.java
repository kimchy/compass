package org.compass.core.config.binding.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class MetaDataReaderFactory {

    private static final Log log = LogFactory.getLog(MetaDataReaderFactory.class);

    /**
     * Returns the {@link MetaDataReader} to use. Might return <code>null</code> if no supported library
     * is found.
     */
    public static MetaDataReader getMetaDataReader(CompassSettings settings) throws CompassException {
        String defaultMetaDataReader = null;
        try {
            ClassUtils.forName("org.objectweb.asm.Type", settings.getClassLoader());
            defaultMetaDataReader = "org.compass.core.config.binding.metadata.AsmMetaDataReader";
        } catch (ClassNotFoundException e) {
            // ASM does not exists
        }
        if (defaultMetaDataReader == null) {
            try {
                ClassUtils.forName("javassist.bytecode.ClassFile", settings.getClassLoader());
                defaultMetaDataReader = "org.compass.core.config.binding.metadata.JavassistMetaDataReader";
            } catch (ClassNotFoundException e) {
                // Javassist does not exists
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Default class meta data reader detected [" + defaultMetaDataReader + "]");
        }
        return (MetaDataReader) settings.getSettingAsInstance(CompassEnvironment.Scanner.READER, defaultMetaDataReader);
    }
}
