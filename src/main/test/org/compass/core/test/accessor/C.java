package org.compass.core.test.accessor;

public class C {

    private Long id;

    private String value;

    protected C() {

    }

    public C(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String getSpecialValue() {
        return this.value + " special";
    }

}
