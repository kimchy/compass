package org.compass.gps.device.hibernate.cascade.one;

/**
 * @author Maurice Nicholson
 */
public class Foundations {
    private Long id;
    private Long version;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
