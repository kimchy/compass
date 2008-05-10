package org.compass.annotations.test.reference.poly;

import java.util.Set;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableReference;

/**
 * A group of Categories (Composite pattern)
 */
@Searchable(poly = true)
public class CategoryGroup extends Category {
    private Set<Category> categories;

    public CategoryGroup() {
        super();
    }

    public CategoryGroup(Long id, String name) {
        super(id, name);
    }

    @SearchableReference
    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
}
