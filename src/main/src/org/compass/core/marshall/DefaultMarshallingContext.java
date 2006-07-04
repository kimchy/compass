package org.compass.core.marshall;

import java.util.HashMap;

import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.impl.ResourceIdKey;

/**
 * @author kimchy
 */
public class DefaultMarshallingContext implements MarshallingContext {

    private CompassMapping mapping;

    private SearchEngine searchEngine;

    private ConverterLookup converterLookup;

    private InternalCompassSession session;

    private MarshallingStrategy marshallingStrategy;

    private HashMap attributes = new HashMap();

    private HashMap nullValuesPath = new HashMap();

    private HashMap unmarshalled = new HashMap();

    public DefaultMarshallingContext(CompassMapping mapping, SearchEngine searchEngine,
                                     ConverterLookup converterLookup, InternalCompassSession session,
                                     MarshallingStrategy marshallingStrategy) {
        this.mapping = mapping;
        this.searchEngine = searchEngine;
        this.converterLookup = converterLookup;
        this.session = session;
        this.marshallingStrategy = marshallingStrategy;
    }

    public void clearContext() {
        this.attributes.clear();
        this.nullValuesPath.clear();
        this.unmarshalled.clear();
    }

    public void setUnmarshalled(ResourceIdKey key, Object obj) {
        unmarshalled.put(key, obj);
        session.getFirstLevelCache().set(key, obj);
    }

    public Object getUnmarshalled(ResourceIdKey key) {
        Object obj = session.getFirstLevelCache().get(key);
        if (obj != null) {
            return obj;
        }
        return unmarshalled.get(key);
    }

    public void setHandleNulls(String path) {
        nullValuesPath.put(path, "");
    }

    public void removeHandleNulls(String path) {
        nullValuesPath.remove(path);
    }

    public boolean handleNulls() {
        return nullValuesPath.size() > 0;
    }

    public ConverterLookup getConverterLookup() {
        return converterLookup;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public CompassMapping getCompassMapping() {
        return mapping;
    }

    public InternalCompassSession getSession() {
        return session;
    }

    public MarshallingStrategy getMarshallingStrategy() {
        return marshallingStrategy;
    }

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

}
