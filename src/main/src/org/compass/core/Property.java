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

package org.compass.core;

import java.io.Serializable;

/**
 * The basic Compass meta data holder. A property is a name value pair representing the mapped object attribute and
 * value. Can be used to fetch meta data values from a resource in an abstract way.
 * <p/>
 * As an example:
 * <p/>
 * <pre>
 * resource.getProperty(&quot;authorName&quot;).getStringValue();
 * </pre>
 * <p/>
 * </p>
 * <p/>
 * Note that there are internal properties (that compass uses for the marshlling of objects) and meta data properties.
 * </p>
 *
 * @author kimchy
 */
public interface Property extends Serializable {

    /**
     * Specifies whether and how a meta-data property will be stored.
     */
    public static enum Store {

        /**
         * Do not store the property value in the index.
         */
        NO,

        /**
         * Store the original property value in the index. This is useful for short texts like a document's title which
         * should be displayed with the results. The value is stored in its original form, i.e. no analyzer is used
         * before it is stored.
         */
        YES,

        /**
         * Store the original property value in the index in a compressed form. This is useful for long documents and
         * for binary valued fields.
         */
        COMPRESS;

        public static String toString(Property.Store propertyStore) {
            if (propertyStore == Property.Store.NO) {
                return "no";
            } else if (propertyStore == Property.Store.YES) {
                return "yes";
            } else if (propertyStore == Property.Store.COMPRESS) {
                return "compress";
            }
            throw new IllegalArgumentException("Can't find property store for [" + propertyStore + "]");
        }

        public static Property.Store fromString(String propertyStore) {
            if (propertyStore == null || "na".equalsIgnoreCase(propertyStore)) {
                return null;
            }
            if ("no".equalsIgnoreCase(propertyStore)) {
                return Property.Store.NO;
            } else if ("yes".equalsIgnoreCase(propertyStore)) {
                return Property.Store.YES;
            } else if ("compress".equalsIgnoreCase(propertyStore)) {
                return Property.Store.COMPRESS;
            }
            throw new IllegalArgumentException("Can't find property store for [" + propertyStore + "]");
        }
    }

    /**
     * Specifies whether and how a meta-data property should be indexed.
     */
    public static enum Index {

        /**
         * Do not index the property value. This property can thus not be searched, but one can still access its
         * contents provided it is {@link Property.Store stored}.
         */
        NO,

        /**
         * @deprecated renamed to {@link #ANALYZED}.
         */
        TOKENIZED,

        /**
         * Index the property's value so it can be searched. An Analyzer will be used to tokenize and possibly further
         * normalize the text before its terms will be stored in the index. This is useful for common text.
         */
        ANALYZED,

        /**
         * @deprecated renamed to {@link #NOT_ANALYZED}
         */
        UN_TOKENIZED,

        /**
         * Index the property's value without using an Analyzer, so it can be searched. As no analyzer is used the value
         * will be stored as a single term. This is useful for unique Ids like product numbers.
         */
        NOT_ANALYZED;

        public static String toString(Property.Index propertyIndex) {
            if (propertyIndex == Property.Index.NO) {
                return "no";
            } else if (propertyIndex == Property.Index.ANALYZED) {
                return "analyzed";
            } else if (propertyIndex == Property.Index.NOT_ANALYZED) {
                return "not_analyzed";
            } else if (propertyIndex == Property.Index.TOKENIZED) {
                return "tokenized";
            } else if (propertyIndex == Property.Index.UN_TOKENIZED) {
                return "un_tokenized";
            }
            throw new IllegalArgumentException("Can't find property index for [" + propertyIndex + "]");
        }

        public static Property.Index fromString(String propertyIndex) {
            if (propertyIndex == null || "na".equalsIgnoreCase(propertyIndex)) {
                return null;
            }
            if ("no".equalsIgnoreCase(propertyIndex)) {
                return Property.Index.NO;
            } else if ("analyzed".equalsIgnoreCase(propertyIndex)) {
                return Property.Index.ANALYZED;
            } else if ("not_analyzed".equalsIgnoreCase(propertyIndex)) {
                return Property.Index.NOT_ANALYZED;
            } else if ("tokenized".equalsIgnoreCase(propertyIndex)) {
                return Property.Index.TOKENIZED;
            } else if ("un_tokenized".equalsIgnoreCase(propertyIndex)) {
                return Property.Index.UN_TOKENIZED;
            }
            throw new IllegalArgumentException("Can't find property index for [" + propertyIndex + "]");
        }
    }

    /**
     * Specifies whether and how a meta-data property should have term vectors.
     */
    public static enum TermVector {

        /**
         * Do not store term vectors.
         */
        NO,

        /**
         * Store the term vectors of each document. A term vector is a list of the document's terms and their number of
         * occurences in that document.
         */
        YES,

        /**
         * Store the term vector + token position information
         *
         * @see #YES
         */
        WITH_POSITIONS,

        /**
         * Store the term vector + Token offset information
         *
         * @see #YES
         */
        WITH_OFFSETS,

        /**
         * Store the term vector + Token position and offset information
         *
         * @see #YES
         * @see #WITH_POSITIONS
         * @see #WITH_OFFSETS
         */
        WITH_POSITIONS_OFFSETS;

        public static String toString(Property.TermVector propertyTermVector) {
            if (propertyTermVector == Property.TermVector.NO) {
                return "no";
            } else if (propertyTermVector == Property.TermVector.YES) {
                return "yes";
            } else if (propertyTermVector == Property.TermVector.WITH_POSITIONS) {
                return "positions";
            } else if (propertyTermVector == Property.TermVector.WITH_OFFSETS) {
                return "offsets";
            } else if (propertyTermVector == Property.TermVector.WITH_POSITIONS_OFFSETS) {
                return "positions_offsets";
            }
            throw new IllegalArgumentException("Can't find property term vector for [" + propertyTermVector + "]");
        }

        public static Property.TermVector fromString(String propertyTermVector) {
            if (propertyTermVector == null || "na".equalsIgnoreCase(propertyTermVector)) {
                return null;
            }
            if ("no".equalsIgnoreCase(propertyTermVector)) {
                return Property.TermVector.NO;
            } else if ("yes".equalsIgnoreCase(propertyTermVector)) {
                return Property.TermVector.YES;
            } else if ("positions".equalsIgnoreCase(propertyTermVector)) {
                return Property.TermVector.WITH_POSITIONS;
            } else if ("offsets".equalsIgnoreCase(propertyTermVector)) {
                return Property.TermVector.WITH_OFFSETS;
            } else if ("positions_offsets".equalsIgnoreCase(propertyTermVector)) {
                return Property.TermVector.WITH_POSITIONS_OFFSETS;
            }
            throw new IllegalArgumentException("Can't find property term vector for [" + propertyTermVector + "]");
        }
    }

    /**
     * Returns the name of the property.
     *
     * @return the name of the property
     */
    String getName();

    /**
     * Returns the string value of the proerty.
     *
     * @return the string value
     */
    String getStringValue();

    /**
     * Returns the object value of the property. If a converter is associated
     * with the property in one of Compass mapping definitions, it will be used
     * to convert the string value to an object value. If there is no converter
     * associated with the property, the string value will be returned.
     *
     * @return The converted object value
     */
    Object getObjectValue();

    /**
     * Returns the binary values of the property. Only valid if <code>isBinary</code> is true.
     *
     * @return the binary value
     */
    byte[] getBinaryValue();

    /**
     * Returns the boost for the property.
     *
     * @return the boost value
     */
    float getBoost();

    /**
     * Sets the boost level for the property. The boost value can be specified in the mapping file to influence the
     * order of search results.
     */
    void setBoost(float boost);

    /**
     * True iff the value of the field is to be indexed, so that it may be searched on.
     */
    boolean isIndexed();

    /**
     * True iff the value of the field is to be stored in the index for return with search hits. It is an error for this
     * to be true if a field is Reader-valued.
     */
    boolean isStored();

    /**
     * True if the value of the field is stored and compressed within the index
     */
    boolean isCompressed();

    /**
     * True iff the value of the field should be tokenized as text prior to indexing. Un-tokenized fields are indexed as
     * a single word and may not be Reader-valued.
     */
    boolean isTokenized();

    /**
     * True iff the term or terms used to index this field are stored as a term vector, available from TODO. These
     * methods do not provide access to the original content of the field, only to terms used to index it. If the
     * original content must be preserved, use the <code>stored</code> attribute instead.
     */
    boolean isTermVectorStored();

    /**
     * True iff the value of the filed is stored as binary
     */
    boolean isBinary();

    /**
     * Expert:
     *
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    boolean isOmitNorms();

    /**
     * Expert:
     *
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field.
     */
    void setOmitNorms(boolean omitNorms);

    /**
     * True if tf is omitted for this indexed field
     */
    boolean isOmitTf();

    /**
     * Expert:
     *
     * If set, omit tf from postings of this indexed field.
     */
    void setOmitTf(boolean omitTf);
}
