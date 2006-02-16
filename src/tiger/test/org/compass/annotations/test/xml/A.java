package org.compass.annotations.test.xml;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    private int id;

    private String value;

    private String valueXml;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SearchableProperty
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueXml() {
        return valueXml;
    }

    public void setValueXml(String valueXml) {
        this.valueXml = valueXml;
    }
}
