package org.compass.core.config.process;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AbstractResourcePropertyMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class RootAliasPostProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        ResourceMapping[] rootMappings = compassMapping.getRootMappings();
        for (int i = 0; i < rootMappings.length; i++) {
            ResourcePropertyMapping[] rpms = rootMappings[i].getResourcePropertyMappings();
            for (int j = 0; j < rpms.length; j++) {
                ((AbstractResourcePropertyMapping) rpms[j]).setRootAlias(rootMappings[i].getAlias());
            }
        }
        return compassMapping;
    }
}