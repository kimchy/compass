package org.compass.core.test.component.inheritance1;

public class DaughterImpl extends ChildImpl implements Daughter {

    private DaughterImpl() {
    }

    public DaughterImpl(String name, Father father) {
        super(name, father);
    }

    public String getName() {
        return super.getName();
    }

    public Father getFather() {
        return super.getFather();
    }
}
