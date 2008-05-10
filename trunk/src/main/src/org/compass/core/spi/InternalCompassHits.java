package org.compass.core.spi;

import org.compass.core.CompassHits;
import org.compass.core.engine.SearchEngineHits;

/**
 * @author kimchy
 */
public interface InternalCompassHits extends CompassHits {

    SearchEngineHits getSearchEngineHits();

    void setHighlightedText(int n, String propertyName, String highlihgtedText);
}
