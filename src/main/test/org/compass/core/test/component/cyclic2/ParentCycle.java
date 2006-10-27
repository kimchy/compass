package org.compass.core.test.component.cyclic2;

import java.util.List;

/**
 * @author kimchy
 */
public class ParentCycle {

    private Long id;

    private String value;

    private List children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
