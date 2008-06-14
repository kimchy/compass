package org.compass.core.converter.mapping.json;

import java.util.Iterator;

import org.compass.core.Resource;
import org.compass.core.json.JsonObject;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonObjectMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class JsonMappingConverterUtils {

    public static boolean marshall(Resource resource, JsonObject jsonObject, JsonObjectMapping mapping, MarshallingContext context) {
        boolean store = false;

        for (Iterator<Mapping> it = mapping.mappingsIt(); it.hasNext();) {
            Mapping m = it.next();
            Object value = jsonObject.opt(m.getName());
            if (value != null && jsonObject.isNullValue(value)) {
                value = null;
            }
            store |= m.getConverter().marshall(resource, value, m, context);
        }

        // still needs to go over unmapped ones...

        return store;
    }
}
