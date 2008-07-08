package org.compass.gps.device.hibernate.cascade.many;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Maurice Nicholson
 */
public class User {
    private Long id;
    private Long version;
    private String name;
    private Set albums;
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

    public Set getAlbums() {
        if (albums == null) {
            albums = new HashSet();
        }
        return albums;
    }

    public void setAlbums(Set albums) {
        this.albums = albums;
    }
}
