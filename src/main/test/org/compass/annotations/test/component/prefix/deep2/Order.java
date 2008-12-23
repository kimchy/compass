package org.compass.annotations.test.component.prefix.deep2;

import java.util.ArrayList;
import java.util.Collection;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false)
public class Order {

    @SearchableId
    private Long id = new Long(-1);

    @SearchableProperty
    private String orderName;

    private Customer customer;

    @SearchableComponent(prefix = "order_")
    private Collection<Item> items = new ArrayList<Item>();

    protected Order() {
    }

    public Order(long id, String orderName, Customer customer) {
        this.id = id;
        this.orderName = orderName;
        this.customer = customer;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
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

}
