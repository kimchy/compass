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

package org.compass.core.lucene.support;

import org.apache.lucene.document.Field;
import org.compass.core.Property;
import org.compass.core.engine.SearchEngineException;

/**
 * A set of helper methods for Lucene {@link org.apache.lucene.document.Field}.
 *
 * @author kimchy
 */
public abstract class FieldHelper {

    public static Field.Index getFieldIndex(Property.Index index) throws SearchEngineException {
        if (index == Property.Index.ANALYZED) {
            return Field.Index.ANALYZED;
        }

        if (index == Property.Index.NOT_ANALYZED) {
            return Field.Index.NOT_ANALYZED;
        }

        if (index == Property.Index.TOKENIZED) {
            return Field.Index.TOKENIZED;
        }

        if (index == Property.Index.UN_TOKENIZED) {
            return Field.Index.UN_TOKENIZED;
        }

        if (index == Property.Index.NO) {
            return Field.Index.NO;
        }

        throw new SearchEngineException("No index type is defined for [" + index + "]");
    }

    public static Field.Store getFieldStore(Property.Store store) throws SearchEngineException {
        if (store == Property.Store.YES) {
            return Field.Store.YES;
        }

        if (store == Property.Store.NO) {
            return Field.Store.NO;
        }

        if (store == Property.Store.COMPRESS) {
            return Field.Store.COMPRESS;
        }

        throw new SearchEngineException("No store type is defined for [" + store + "]");
    }

    public static Field.TermVector getFieldTermVector(Property.TermVector termVector) throws SearchEngineException {
        if (termVector == Property.TermVector.NO) {
            return Field.TermVector.NO;
        }

        if (termVector == Property.TermVector.YES) {
            return Field.TermVector.YES;
        }

        if (termVector == Property.TermVector.WITH_OFFSETS) {
            return Field.TermVector.WITH_OFFSETS;
        }

        if (termVector == Property.TermVector.WITH_POSITIONS) {
            return Field.TermVector.WITH_POSITIONS;
        }

        if (termVector == Property.TermVector.WITH_POSITIONS_OFFSETS) {
            return Field.TermVector.WITH_POSITIONS_OFFSETS;
        }

        throw new SearchEngineException("No term vector type is defined for [" + termVector + "]");
    }
}
