package org.compass.annotations.test.simple;

import org.compass.annotations.Searchable;

/**
 * @author kimchy
 */
@Searchable
public class CImpl implements CInterface {

    private int id;

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
