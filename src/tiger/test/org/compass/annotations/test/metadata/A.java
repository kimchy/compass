package org.compass.annotations.test.metadata;

import org.compass.annotations.*;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    private int id;

    @SearchableProperty
    @SearchableMetaDatas({@SearchableMetaData(name = "value1"), @SearchableMetaData(name = "value2")})
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
