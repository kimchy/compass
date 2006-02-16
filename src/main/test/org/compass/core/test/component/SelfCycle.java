package org.compass.core.test.component;

/**
 * @author kimchy
 */
public class SelfCycle {

    private Long id;

    private SelfCycle selfCycle;

    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SelfCycle getSelfCycle() {
        return selfCycle;
    }

    public void setSelfCycle(SelfCycle selfCycle) {
        this.selfCycle = selfCycle;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
