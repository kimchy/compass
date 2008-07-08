package org.compass.gps.device.hibernate.cascade.inheritance;

import java.util.Set;

/**
 * @author Maurice Nicholson
 */
public class User {
    public Long id;
    public Long version;
    public String name;
    public Set favouritePlaces;
}
