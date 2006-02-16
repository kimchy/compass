package org.compass.annotations.test.simple;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class B extends A {

    private String value1;

    @SearchableProperty
    public String getValue1() {
        return value1;
    }

    public void setValue1(String value) {
        this.value1 = value;
    }
}
