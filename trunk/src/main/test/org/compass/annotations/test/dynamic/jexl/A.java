package org.compass.annotations.test.dynamic.jexl;

import java.util.Date;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableDynamicMetaData;
import org.compass.annotations.SearchableDynamicMetaDatas;
import org.compass.annotations.SearchableId;

/**
 * @author kimchy
 */
@Searchable

@SearchableDynamicMetaData(name = "test", expression = "data.value + data.value2", converter = "jexl")

@SearchableDynamicMetaDatas({
@SearchableDynamicMetaData(name = "date", expression = "data.date", converter = "jexl", format = "yyyy", type = Date.class),
@SearchableDynamicMetaData(name = "test2", expression = "data.value", converter = "jexl")
        })
public class A {

    @SearchableId
    private Integer id;

    private String value;

    private String value2;

    private Date date;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
