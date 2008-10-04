package org.compass.core.converter.dynamic;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.converter.basic.FormatConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * A simple base class for {@link DynamicConverter}. Requires implementation of
 * {@link #evaluate(Object,org.compass.core.mapping.ResourcePropertyMapping)}.
 *
 * <p>Also holds a {@link FormatConverter} for expression that return formatable
 * objects (like Date).
 *
 * @author kimchy
 */
public abstract class AbstractDynamicConverter extends AbstractBasicConverter implements DynamicConverter {

    private FormatConverter formatConverter;

    private Class type;

    public DynamicConverter copy() {
        try {
            DynamicConverter converter = getClass().newInstance();
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

        if (root == null) {
            return false;
        }
        Object value = evaluate(root, resourcePropertyMapping);
        if (value == null) {
            if (resourcePropertyMapping.hasNullValue()) {
                addProperty(resourcePropertyMapping.getNullValue(), resourcePropertyMapping, root, context, resource);
            }
            return false;
        }
        // save the value in the search engine. Handle array/collection and single values
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addProperty(Array.get(value, i), resourcePropertyMapping, root, context, resource);
            }
        } else if (value instanceof Collection) {
            Collection colValues = (Collection) value;
            for (Iterator it = colValues.iterator(); it.hasNext();) {
                addProperty(it.next(), resourcePropertyMapping, root, context, resource);
            }
        } else {
            addProperty(value, resourcePropertyMapping, root, context, resource);
        }
        return resourcePropertyMapping.getStore() != Property.Store.NO;
    }

    protected void addProperty(Object value, ResourcePropertyMapping resourcePropertyMapping,
                               Object root, MarshallingContext context, Resource resource) {
        String sValue;
        if (formatConverter == null) {
            sValue = value.toString();
        } else {
            sValue = formatConverter.toString(value, resourcePropertyMapping);
        }
        addProperty(sValue, resourcePropertyMapping, root, context, resource);
    }

    private void addProperty(String value, ResourcePropertyMapping resourcePropertyMapping,
                             Object root, MarshallingContext context, Resource resource) {
        Property p = context.getResourceFactory().createProperty(value, resourcePropertyMapping);
        doSetBoost(p, root, resourcePropertyMapping, context);
        resource.addProperty(p);
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
    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        // do nothing here
        return null;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public Property.Index suggestIndex() {
        if (formatConverter != null) {
            return formatConverter.suggestIndex();
        }
        return super.suggestIndex();
    }

    public Property.TermVector suggestTermVector() {
        if (formatConverter != null) {
            return formatConverter.suggestTermVector();
        }
        return super.suggestTermVector();
    }

    public Property.Store suggestStore() {
        if (formatConverter != null) {
            return formatConverter.suggestStore();
        }
        return super.suggestStore();
    }

    public Boolean suggestOmitNorms() {
        if (formatConverter != null) {
            return formatConverter.suggestOmitNorms();
        }
        return super.suggestOmitNorms();
    }

    public Boolean suggestOmitTf() {
        if (formatConverter != null) {
            return formatConverter.suggestOmitTf();
        }
        return super.suggestOmitTf();
    }
}
