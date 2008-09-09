package org.compass.core.test.component.simplenum;

/**
 * @author kimchy
 */
public enum B {

    TEST1(1),
    TEST2(2);

    private int value;

    B() {

    }

    B(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
