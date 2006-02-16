package org.compass.annotations.test.extend;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;

/**
 * @author kimchy
 */
@Searchable(extend = {"A-contract", "A-contract2"})
public class A {

    @SearchableId
    private int id;

    private String value;

    private String value2;

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

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
