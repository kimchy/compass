package org.compass.annotations.test.component.prefix.deep2;

import java.util.ArrayList;
import java.util.Collection;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@Searchable
public class Customer {

    @SearchableId
    private Long id = new Long(-1);

    @SearchableProperty(name = "customer_name")
    private String name;

    @SearchableComponent
    private Collection<Order> orders = new ArrayList<Order>();

    @SearchableComponent
    private Collection<WishList> wishLists = new ArrayList<WishList>();

    protected Customer() {
    }

    public Customer(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Order addOrder(long id, String orderId) {
        Order order = new Order(id, orderId, this);
        this.orders.add(order);
        return order;
    }

    public WishList addWishList(long id, String wishListName) {
        WishList wishList = new WishList(id, wishListName, this);
        this.wishLists.add(wishList);
        return wishList;
    }

}
