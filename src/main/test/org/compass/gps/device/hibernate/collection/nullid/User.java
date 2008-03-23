package org.compass.gps.device.hibernate.collection.nullid;

import java.util.HashSet;
import java.util.Set;

public class User {
    private Long id;
    private Long version;
    private String name;
    private Set<Album> albums;

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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Album> getAlbums() {
        if (albums == null) {
            albums = new HashSet<Album>();
        }
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
    }
}
