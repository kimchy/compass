package org.compass.core.converter.mapping.json;

import java.util.Map;

import org.compass.core.Resource;
import org.compass.core.json.JsonObject;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MultipleMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class JsonMappingConverterUtils {

    public static boolean marshall(Resource resource, JsonObject jsonObject, MultipleMapping mapping, MarshallingContext context) {
        boolean store = false;
        for (Map.Entry<String, Object> entry : jsonObject.entries().entrySet()) {
            Object value = entry.getValue();
            if (jsonObject.isNullValue(value)) {
                value = null;
            }
            Mapping entryMapping = mapping.getMapping(entry.getKey());
            if (entryMapping == null) {
                // we need to support dynamic creation as well
                continue;
            } else {
                store |= entryMapping.getConverter().marshall(resource, value, entryMapping, context);
            }
        }
        return store;
    }
}
