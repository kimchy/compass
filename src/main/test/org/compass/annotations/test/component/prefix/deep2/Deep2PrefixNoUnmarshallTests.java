package org.compass.annotations.test.component.prefix.deep2;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public class Deep2PrefixNoUnmarshallTests extends AbstractAnnotationsTestCase {

    @Override
    protected void addSettings(CompassSettings settings) {
        settings.setBooleanSetting(CompassEnvironment.Osem.SUPPORT_UNMARSHALL, false);
    }

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(Customer.class).addClass(Item.class).addClass(Order.class).addClass(WishList.class);
    }

    public void testDeepPrefixComponent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Customer customer = new Customer(1, "Jon Doe");
        Order order = customer.addOrder(1, "order 1");
        order.addItem(1, "item 1");
        order.addItem(2, "item 2");
        order = customer.addOrder(2, "order 2");
        order.addItem(3, "item 3");
        order.addItem(4, "item 4");
        WishList wishList = customer.addWishList(1, "wish list 1");
        wishList.addItem(5, "item 5");
        wishList.addItem(6, "item 6");
        wishList = customer.addWishList(2, "wish list 2");
        wishList.addItem(7, "item 7");
        wishList.addItem(8, "item 8");

        session.save(customer);

        Resource resource = session.loadResource(Customer.class, 1);
        assertEquals(4, resource.getProperties("order_itemName").length);
        assertEquals(0, resource.getProperties("order_order_itemName").length);
        assertEquals(4, resource.getProperties("wishlist_itemName").length);
        assertEquals(0, resource.getProperties("wishlist_wishlist_itemName").length);

        tr.commit();
        session.close();
    }
}