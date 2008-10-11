package org.compass.annotations.test.component.prefix.simple2;

import java.util.LinkedHashSet;
import java.util.Set;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.Store;
import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class Simple2PrefixTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(Organisation.class).addClass(Contact.class).addClass(VisitAddress.class).addClass(Address.class).addClass(MailAddress.class);
    }

    public void testSimple2Prefix() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Organisation org = new Organisation();
        org.id = 1;
        org.mainContact.visitAddress = new VisitAddress();
        org.mainContact.visitAddress.street = "mainContactVisitAddress";
        org.mainContact.mailAddress = new MailAddress();
        org.mainContact.mailAddress.street = "mainContactMailAddress";

        Contact c1 = new Contact();
        c1.visitAddress = new VisitAddress();
        c1.visitAddress.street = "extrac1visitAddress";
        c1.mailAddress = new MailAddress();
        c1.mailAddress.street = "extrac1mailAddress";
        org.extraContact.add(c1);
        Contact c2 = new Contact();
        c2.visitAddress = new VisitAddress();
        c2.visitAddress.street = "extrac2visitAddress";
        c2.mailAddress = new MailAddress();
        c2.mailAddress.street = "extrac2mailAddress";
        org.extraContact.add(c2);

        session.save(org);

        assertEquals(1, session.find("main-visit-street:mainContactVisitAddress").length());
        assertEquals(1, session.find("main-mail-street:mainContactMailAddress").length());
        assertEquals(1, session.find("extra-visit-street:extrac1visitAddress").length());
        assertEquals(1, session.find("extra-mail-street:extrac1mailAddress").length());
        assertEquals(1, session.find("extra-visit-street:extrac2visitAddress").length());
        assertEquals(1, session.find("extra-mail-street:extrac2mailAddress").length());

        tr.commit();
        session.close();
    }

    @Searchable
    public static class Organisation {
        @SearchableId
        int id;

        @SearchableComponent(prefix = "main-")
        Contact mainContact = new Contact();

        @SearchableComponent(prefix = "extra-")
        Set<Contact> extraContact = new LinkedHashSet<Contact>();
    }

    @Searchable(root = false)
    public static class Contact {
        @SearchableComponent(prefix = "visit-")
        VisitAddress visitAddress;

        @SearchableComponent(prefix = "mail-")
        MailAddress mailAddress;
    }

    @Searchable(root = false)
    public static class VisitAddress extends Address {
        @SearchableProperty(store = Store.NO)
        String route;
    }

    @Searchable(root = false)
    public static class MailAddress extends Address {
        @SearchableProperty(store = Store.NO)
        Integer postbox;
    }

    @Searchable(root = false)
    public static class Address {
        @SearchableProperty(store = Store.NO)
        String street;
    }
}
