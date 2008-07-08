package org.compass.gps.device.hibernate.cascade.many;

/**
 * @author Maurice Nicholson
 */
public class Album {
    private Long id;
    private Long version;
    private String title;
    private User owner;
    public boolean throwError;

//    static belongsTo= User

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        if (throwError) {
            throw new RuntimeException("This is the error you asked me to throw");
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
