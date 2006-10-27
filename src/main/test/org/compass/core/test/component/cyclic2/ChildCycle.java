package org.compass.core.test.component.cyclic2;

import java.util.List;

/**
 * @author kimchy
 */
public class ChildCycle {

    private List children;

    private String value;

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
