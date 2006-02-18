package org.compass.core.test.component;

/**
 * @author kimchy
 */
public class SimpleRootId {

    private Long id;

    private String value;

    private SimpleComponentId firstComponent;

    private SimpleComponentId secondComponent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SimpleComponentId getSecondComponent() {
        return secondComponent;
    }

    public void setSecondComponent(SimpleComponentId comp) {
        this.secondComponent = comp;
    }

    public SimpleComponentId getFirstComponent() {
        return firstComponent;
    }

    public void setFirstComponent(SimpleComponentId comp) {
        this.firstComponent = comp;
    }
}
