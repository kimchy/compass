package org.compass.core.test.collection;

import java.util.Collection;

/**
 * @author kimchy
 */
public class SimpleTypeCollection {

    private Long id;

    private String value;

    private Collection strings;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the strings.
     */
    public Collection getStrings() {
        return strings;
    }

    /**
     * @param strings
     *            The strings to set.
     */
    public void setStrings(Collection strings) {
        this.strings = strings;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
