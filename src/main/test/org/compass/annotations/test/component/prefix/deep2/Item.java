package org.compass.annotations.test.component.prefix.deep2;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false)
public class Item {

    @SearchableId
    private Long id = new Long(-1);

    @SearchableProperty
    private String itemName;

    protected Item() {
    }

    public Item(long id, String itemName) {
        this.id = id;
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getId() {
        return id;
    }

}
