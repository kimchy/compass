package org.compass.core.mapping.internal;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;

/**
 * @author kimchy
 */
public interface InternalCompassMapping extends CompassMapping {

    void addMapping(AliasMapping mapping) throws MappingException;

    void clearMappings();

    void postProcess();

    InternalCompassMapping copy(ConverterLookup converterLookup);

    void setPath(PropertyPath path);
}
