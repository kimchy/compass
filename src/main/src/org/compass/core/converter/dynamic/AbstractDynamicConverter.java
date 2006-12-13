package org.compass.core.converter.dynamic;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.converter.basic.FormatConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A simple base class for {@link DynamicConverter}. Requires implementation of
 * {@link #evaluate(Object,org.compass.core.mapping.ResourcePropertyMapping)}.
 * <p/>
 * Also holds a {@link FormatConverter} for expression that return formatable
 * objects (like Date).
 *
 * @author kimchy
 */
public abstract class AbstractDynamicConverter extends AbstractBasicConverter implements DynamicConverter {

    private FormatConverter formatConverter;

    private Class type;

    public DynamicConverter copy() {
        try {
            DynamicConverter converter = (DynamicConverter) getClass().newInstance();
            converter.setType(getType());
            converter.setFormatConverter(formatConverter);
            return converter;
        } catch (Exception e) {
            throw new ConversionException("This should not happen", e);
        }
    }

    public void setFormatConverter(FormatConverter formatConverter) {
        this.formatConverter = formatConverter;
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }
        String sValue = getNullValue(context);
        if (root != null) {
            Object value = evaluate(root, resourcePropertyMapping);
            if (value == null) {
                return false;
            } else {
                if (formatConverter == null) {
                    sValue = value.toString();
                } else {
                    sValue = formatConverter.toString(value, resourcePropertyMapping);
                }
            }
        }
        Property p = searchEngine.createProperty(sValue, resourcePropertyMapping);
        doSetBoost(p, root, resourcePropertyMapping, context);
        resource.addProperty(p);

        return resourcePropertyMapping.getStore() != Property.Store.NO;
    }

    /**
     * Evaluates the given data object using the configured expression.
     *
     * @param o                       The data object
     * @param resourcePropertyMapping The resource mapping
     * @return The object returned as a result of expression evaluation
     * @throws ConversionException
     */
    protected abstract Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;

    /**
     * Does nothing since there is no meaning for un-marshalling for dynamic converters
     */
    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        // nothing to do here
        return null;
    }

    /**
     * Does nothing since there is no meaning for un-marshalling for dynamic converters
     */
    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        // do nothing here
        return null;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
