package org.compass.annotations.test.id;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId(name = "test")
    private int id;

    private String value;

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
}
