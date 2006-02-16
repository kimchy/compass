package org.compass.annotations.test.overrideannotations;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SearchableProperty(name = "annValue")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
