package org.compass.core.test.termfreqs.simple;

/**
 * @author kimchy
 */
public class A {

    int id;

    String value;

    String name;

    A() {

    }

    public A(int id, String value) {
        this(id, value, null);
    }

    public A(int id, String value, String name) {
        this.id = id;
        this.value = value;
        this.name = name;
    }
}
