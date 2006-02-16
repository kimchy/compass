package org.compass.gps.device.jpa.model;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * @author kimchy
 */
@Entity
@Searchable(subIndex = "simple1")
public class SimpleExtend extends SimpleBase implements Serializable {

    @SearchableProperty
    @Basic
    private String valueExtend;

    public String getValueExtend() {
        return valueExtend;
    }

    public void setValueExtend(String valueExtend) {
        this.valueExtend = valueExtend;
    }
}
