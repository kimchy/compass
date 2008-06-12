package org.compass.core.converter.json;

import org.compass.core.converter.ConversionException;
import org.compass.core.json.AliasedJsonObject;
import org.compass.core.json.JsonObject;

/**
 * Converts an {@link org.compass.core.json.JsonObject} to and from an JSON string.
 *
 * @author kimchy
 */
public interface JsonContentConverter {

    /**
     * Converts an {@link org.compass.core.xml.XmlObject} into an xml string.
     *
     * @param jsonObject The JSON object to convert to a JSON string
     * @return A JSON string representation of the Json object
     * @throws org.compass.core.converter.ConversionException
     *          Failed to convert the JSON object to a JSON string
     */
    String toJSON(JsonObject jsonObject) throws ConversionException;

    /**
     * Converts a JSON string into an {@link org.compass.core.json.AliasedJsonObject}.
     *
     * @param alias The alias the aliased JSON object is associated with
     * @param json  The JSON string that will be converted into an aliases JSON object
     * @return The aliased JSON object that is the restult of the JSON parsed
     * @throws org.compass.core.converter.ConversionException
     *
     */
    AliasedJsonObject fromJSON(String alias, String json) throws ConversionException;

}