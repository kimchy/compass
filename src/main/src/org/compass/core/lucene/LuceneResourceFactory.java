package org.compass.core.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.engine.RepeatableReader;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.support.FieldHelper;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.ReverseType;
import org.compass.core.util.StringUtils;
import org.compass.core.util.reader.ReverseStringReader;

/**
 * @author kimchy
 */
public class LuceneResourceFactory implements ResourceFactory {

    private LuceneSearchEngineFactory searchEngineFactory;

    public LuceneResourceFactory(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public String getNullValue() {
        return "";
    }

    public boolean isNullValue(String value) {
        return value == null || value.length() == 0;
    }

    public Resource createResource(String alias) throws SearchEngineException {
        return new LuceneMultiResource(alias, searchEngineFactory);
    }

    public Property createProperty(String value, ResourcePropertyMapping mapping) throws SearchEngineException {
        return createProperty(mapping.getPath().getPath(), value, mapping);
    }

    public Property createProperty(String value, ResourcePropertyMapping mapping,
                                   Property.Store store, Property.Index index) throws SearchEngineException {
        return createProperty(mapping.getPath().getPath(), value, mapping, store, index);
    }

    public Property createProperty(String name, String value, ResourcePropertyMapping mapping) throws SearchEngineException {
        return createProperty(name, value, mapping, mapping.getStore(), mapping.getIndex());
    }

    public Property createProperty(String name, String value, ResourcePropertyConverter converter) {
        Property.Store store = converter.suggestStore();
        if (store == null) {
            store = Property.Store.YES;
        }
        Property.Index index = converter.suggestIndex();
        if (index == null) {
            index = Property.Index.ANALYZED;
        }
        Property.TermVector termVector = converter.suggestTermVector();
        if (termVector == null) {
            termVector = Property.TermVector.NO;
        }
        Property property = createProperty(name, value, store, index, termVector);
        if (converter.suggestOmitNorms() != null) {
            property.setOmitNorms(converter.suggestOmitNorms());
        }
        if (converter.suggestOmitTf() != null) {
            property.setOmitTf(converter.suggestOmitTf());
        }
        return property;
    }

    public Property createProperty(String name, String value, ResourcePropertyMapping mapping,
                                   Property.Store store, Property.Index index) throws SearchEngineException {
        Property property;
        if (mapping.getReverse() == ReverseType.NO) {
            property = createProperty(name, value, store, index, mapping.getTermVector());
        } else if (mapping.getReverse() == ReverseType.READER) {
            property = createProperty(name, new ReverseStringReader(value), mapping.getTermVector());
        } else if (mapping.getReverse() == ReverseType.STRING) {
            property = createProperty(name, StringUtils.reverse(value), store, index, mapping.getTermVector());
        } else {
            throw new SearchEngineException("Unsupported Reverse type [" + mapping.getReverse() + "]");
        }
        property.setBoost(mapping.getBoost());
        if (mapping.isOmitNorms() != null) {
            property.setOmitNorms(mapping.isOmitNorms());
        }
        if (mapping.isOmitTf() != null) {
            property.setOmitTf(mapping.isOmitTf());
        }
        property.setBoost(mapping.getBoost());
        ((LuceneProperty) property).setPropertyMapping(mapping);
        return property;
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index)
            throws SearchEngineException {
        return createProperty(name, value, store, index, Property.TermVector.NO);
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index,
                                   Property.TermVector termVector) throws SearchEngineException {
        Field.Store fieldStore = FieldHelper.getFieldStore(store);
        Field.Index fieldIndex = FieldHelper.getFieldIndex(index);
        Field.TermVector fieldTermVector = FieldHelper.getFieldTermVector(termVector);
        Field field = new Field(name, value, fieldStore, fieldIndex, fieldTermVector);
        return new LuceneProperty(field);
    }

    public Property createProperty(String name, TokenStream tokenStream, Property.TermVector termVector) {
        Field.TermVector fieldTermVector = FieldHelper.getFieldTermVector(termVector);
        Field field = new Field(name, tokenStream, fieldTermVector);
        return new LuceneProperty(field);
    }

    public Property createProperty(String name, Reader value) {
        return createProperty(name, value, Property.TermVector.NO);
    }

    public Property createProperty(String name, byte[] value, Property.Store store) throws SearchEngineException {
        Field.Store fieldStore = FieldHelper.getFieldStore(store);
        Field field = new Field(name, value, fieldStore);
        return new LuceneProperty(field);
    }

    public Property createProperty(String name, Reader value, Property.TermVector termVector) {
        Field.TermVector fieldTermVector = FieldHelper.getFieldTermVector(termVector);
        Field field = new Field(name, value, fieldTermVector);
        if (value instanceof RepeatableReader) {
            return new LuceneProperty(field, (RepeatableReader) value);
        }
        return new LuceneProperty(field);
    }
}
