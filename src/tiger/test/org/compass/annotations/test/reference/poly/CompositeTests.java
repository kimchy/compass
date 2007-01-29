package org.compass.annotations.test.reference.poly;

import java.util.HashSet;
import java.util.Set;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * Composite tests
 */
public class CompositeTests extends AbstractAnnotationsTestCase {


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(Category.class).addClass(CategoryGroup.class);
    }

    public void testCompositeRelationships() {
        // Build model
        CategoryGroup sports = new CategoryGroup(10l, "Sports");
        Set<Category> categories = new HashSet<Category>();
        categories.add(new Category(100l, "Fishing"));
        categories.add(new Category(101l, "Golf"));
        categories.add(new Category(102l, "Extreme Ironing"));
        sports.setCategories(categories);

        assertSportsCategoryGroup(sports);

        // Index
        CompassSession compassSession = openSession();
        CompassTransaction compassTransaction = compassSession.beginTransaction();

        compassSession.save(sports);

        compassTransaction.commit();
        compassSession.close();

        // Search
        compassSession = openSession();
        compassTransaction = compassSession.beginTransaction();
        CategoryGroup sportsLoaded = (CategoryGroup) compassSession.get(CategoryGroup.class, 10l);
        compassTransaction.commit();
        compassSession.close();

        assertSportsCategoryGroup(sportsLoaded);
    }

    private void assertSportsCategoryGroup(CategoryGroup sports) {
        assertNotNull(sports);
        assertEquals("Sports", sports.getName());
        assertNotNull(sports.getCategories());
        assertEquals(3, sports.getCategories().size());
    }
}
