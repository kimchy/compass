/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.json.jackson.converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.BaseMapper;
import org.codehaus.jackson.map.JsonMappingException;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.json.JsonContentConverter;
import org.compass.core.json.AliasedJsonObject;
import org.compass.core.json.JsonArray;
import org.compass.core.json.JsonObject;
import org.compass.core.json.jackson.JacksonAliasedJsonObject;
import org.compass.core.json.jackson.JacksonJsonArray;
import org.compass.core.json.jackson.JacksonJsonObject;
import org.compass.core.util.StringBuilderWriter;

/**
 * A Jackson based json converter.
 *
 * @author kimchy
 */
public class JacksonContentConverter implements JsonContentConverter {

    private static JsonFactory jsonFactory = new JsonFactory();

    private static ContentMapper mapper = new ContentMapper(jsonFactory);


    public String toJSON(JsonObject jsonObject) throws ConversionException {
        StringBuilderWriter sbWriter = StringBuilderWriter.Cached.cached();
        try {
            JsonGenerator generator = jsonFactory.createJsonGenerator(sbWriter);
            generateJsonObject(jsonObject, generator);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert json to string", e);
        }
        return sbWriter.getBuilder().toString();
    }

    public AliasedJsonObject fromJSON(String alias, String json) throws ConversionException {
        try {
            JacksonJsonObject jsonObject = mapper.readTree(json);
            return new JacksonAliasedJsonObject(alias, jsonObject.getNodes());
        } catch (Exception e) {
            throw new ConversionException("Failed to convert json: " + json + " with alias [" + alias + "]", e);
        }
    }

    /**
     * Uses Jackson {@link org.codehaus.jackson.JsonGenerator} in order to generate the json string based on
     * a {@link org.compass.core.json.JsonObject}.
     */
    private void generateJsonObject(JsonObject jsonObject, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext();) {
            String key = keyIt.next();
            Object value = jsonObject.opt(key);
            if (value == null) {
                generator.writeNullField(key);
            } else if (value instanceof String) {
                generator.writeStringField(key, (String) value);
            } else if (value instanceof Integer) {
                generator.writeNumberField(key, (Integer) value);
            } else if (value instanceof Long) {
                generator.writeNumberField(key, (Long) value);
            } else if (value instanceof Double) {
                generator.writeNumberField(key, (Double) value);
            } else if (value instanceof Float) {
                generator.writeNumberField(key, (Float) value);
            } else if (value instanceof BigDecimal) {
                generator.writeNumberField(key, (BigDecimal) value);
            } else if (value instanceof JsonObject) {
                generateJsonObject((JsonObject) value, generator);
            } else if (value instanceof JsonArray) {
                generateJsonArray((JsonArray) value, generator);
            }
        }
        generator.writeEndObject();
    }

    private void generateJsonArray(JsonArray jsonArray, JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.isNull(i)) {
                generator.writeNull();
            }
            Object value = jsonArray.opt(i);
            if (value == null) {
                generator.writeNull();
            } else if (value instanceof String) {
                generator.writeString((String) value);
            } else if (value instanceof Integer) {
                generator.writeNumber((Integer) value);
            } else if (value instanceof Long) {
                generator.writeNumber((Long) value);
            } else if (value instanceof Double) {
                generator.writeNumber((Double) value);
            } else if (value instanceof Float) {
                generator.writeNumber((Float) value);
            } else if (value instanceof BigDecimal) {
                generator.writeNumber((BigDecimal) value);
            } else if (value instanceof JsonObject) {
                generateJsonObject((JsonObject) value, generator);
            } else if (value instanceof JsonArray) {
                generateJsonArray((JsonArray) value, generator);
            }
        }
        generator.writeEndArray();
    }


    /**
     * A mapper from Json content (a String for example) to a {@link org.compass.core.json.jackson.JacksonJsonObject}.
     */
    public static class ContentMapper extends BaseMapper {

        public ContentMapper(JsonFactory jsonFactory) {
            super(jsonFactory);
        }

        public JacksonJsonObject readTree(String jsonContent)
                throws IOException, JsonParseException {
            return _readMapAndClose(_jsonFactory.createJsonParser(jsonContent));
        }

        protected JacksonJsonObject _readMapAndClose(JsonParser jp)
                throws IOException, JsonParseException {
            try {
                return readTree(jp);
            } finally {
                try {
                    jp.close();
                } catch (IOException ioe) {
                }
            }
        }

        /**
         * Method that will use the current event of the underlying parser
         * (and if there's no event yet, tries to advance to an event)
         * to construct a node, and advance the parser to point to the
         * next event, if any. For structured tokens (objects, arrays),
         * will recursively handle and construct contained nodes.
         */
        public JacksonJsonObject readTree(JsonParser jp)
                throws IOException, JsonParseException, JsonMappingException {
            JsonToken curr = jp.getCurrentToken();
            if (curr == null) {
                curr = jp.nextToken();
                // We hit EOF? Nothing more to do, if so:
                if (curr == null) {
                    return null;
                }
            }

            JacksonJsonObject result = (JacksonJsonObject) readAndMap(jp, curr);

            /* Need to also advance the reader, if we get this far,
             * to allow handling of root level sequence of values
             */
            jp.nextToken();
            return result;
        }


        protected Object readAndMap(JsonParser jp, JsonToken currToken)
                throws IOException, JsonParseException {
            switch (currToken) {
                case START_OBJECT: {
                    Map<String, Object> nodes = new HashMap<String, Object>();
                    while ((currToken = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (currToken != JsonToken.FIELD_NAME) {
                            _reportProblem(jp, "Unexpected token (" + currToken + "), expected FIELD_NAME");
                        }
                        String fieldName = jp.getText();
                        Object value = readAndMap(jp, jp.nextToken());

                        if (_cfgDupFields == BaseMapper.DupFields.ERROR) {
                            Object old = nodes.put(fieldName, value);
                            if (old != null) {
                                _reportProblem(jp, "Duplicate value for field '" + fieldName + "', when dup fields mode is " + _cfgDupFields);
                            }
                        } else if (_cfgDupFields == BaseMapper.DupFields.USE_LAST) {
                            // Easy, just add
                            nodes.put(fieldName, value);
                        } else { // use first; need to ensure we don't yet have it
                            if (nodes.get(fieldName) == null) {
                                nodes.put(fieldName, value);
                            }
                        }
                    }
                    return new JacksonJsonObject(nodes);
                }

                case START_ARRAY: {
                    List<Object> values = new ArrayList<Object>();
                    while ((currToken = jp.nextToken()) != JsonToken.END_ARRAY) {
                        Object value = readAndMap(jp, currToken);
                        values.add(value);
                    }
                    return new JacksonJsonArray(values);
                }

                case VALUE_STRING:
                    return jp.getText();

                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    return jp.getNumberValue();

                case VALUE_TRUE:
                    return Boolean.TRUE;

                case VALUE_FALSE:
                    return Boolean.FALSE;

                case VALUE_NULL:
                    return null;

                // These states can not be mapped; input stream is
                // off by an event or two

                case FIELD_NAME:
                case END_OBJECT:
                case END_ARRAY:
                    _reportProblem(jp, "Can not map token " + currToken + ": stream off by a token or two?");

                default: // sanity check, should never happen
                    _throwInternal("Unrecognized event type: " + currToken);
                    return null; // never gets this far
            }
        }

    }
}