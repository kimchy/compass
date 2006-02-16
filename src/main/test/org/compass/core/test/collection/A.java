package org.compass.core.test.collection;

import java.util.Collection;

/**
 * @author kimchy
 */
public class A {

    private Long id;

    private String value;

    private Collection cb;

    /**
     * @return Returns the cb.
     */
    public Collection getCb() {
        return cb;
    }

    /**
     * @param cb
     *            The cb to set.
     */
    public void setCb(Collection cb) {
        this.cb = cb;
    }

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
