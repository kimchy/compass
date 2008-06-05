package org.compass.core.config.binding;

import java.io.InputStream;

import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.binding.metadata.ClassMetaData;
import org.compass.core.config.binding.metadata.MetaDataReader;
import org.compass.core.config.binding.metadata.MetaDataReaderFactory;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public abstract class AbstractClassMetaDataMappingBinding extends AbstractInputStreamMappingBinding {

    private MetaDataReader metaDataReader;

    public void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        super.setUpBinding(mapping, metaData, settings);
        metaDataReader = MetaDataReaderFactory.getMetaDataReader(settings);
        if (metaDataReader == null) {
            log.debug("No meta data reader found, automatic detection is disabled");
        }
    }

    protected boolean doAddInputStream(InputStream is, String resourceName) throws ConfigurationException, MappingException {
        if (metaDataReader == null) {
            return false;
        }
        ClassMetaData classMetaData = metaDataReader.getClassMetaData(is, resourceName);
        if (!isApplicable(classMetaData)) {
            return false;
        }
        try {
            addClass(ClassUtils.forName(classMetaData.getClassName(), settings.getClassLoader()));
        } catch (ClassNotFoundException e) {
            throw new MappingException("Failed to find class [" + classMetaData.getClassName() + "]", e);
        }
        return true;
    }

    protected abstract boolean isApplicable(ClassMetaData classMetaData);

    public String[] getSuffixes() {
        return new String[] {".class"};
    }
}
