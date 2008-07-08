package org.compass.gps.device.hibernate.cascade.one;

/**
 * Created by IntelliJ IDEA.
 * User: mauricenicholson
 * Date: Apr 7, 2008
 * Time: 9:41:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Roof {
    private Long id;
    private Long version;
    private String name;
    private House house;
    public boolean throwError;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        if (throwError) {
            throw new RuntimeException("This is the error you asked me to throw");
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }
}
