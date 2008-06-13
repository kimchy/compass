package org.compass.core.mapping.json;

import org.compass.core.mapping.Mapping;

/**
 * @author kimchy
 */
public interface JsonArrayMapping extends Mapping {

    Mapping getElementMapping();
}
