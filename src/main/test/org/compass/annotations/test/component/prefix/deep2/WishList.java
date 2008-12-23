package org.compass.annotations.test.component.prefix.deep2;

import java.util.ArrayList;
import java.util.Collection;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false)
public class WishList {

    @SearchableId
    private Long id = new Long(-1);

    @SearchableProperty
    private String wishListName;

    private Customer customer;

    @SearchableComponent(prefix = "wishlist_")
    private Collection<Item> items = new ArrayList<Item>();

    protected WishList() {
    }

    public WishList(long id, String wishListName, Customer customer) {
        this.id = id;
        this.wishListName = wishListName;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public Item addItem(long id, String itemName) {
        Item item = new Item(id, itemName);
        items.add(item);
        return item;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getWishListName() {
        return wishListName;
    }

    public void setWishListName(String wishListName) {
        this.wishListName = wishListName;
    }

}
