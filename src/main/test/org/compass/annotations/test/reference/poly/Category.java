package org.compass.annotations.test.reference.poly;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * A named Category
 */
@Searchable(poly = true)
public class Category {
    private Long id;
    private String name;

    public Category() {
        super();
    }

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @SearchableId
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @SearchableProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
