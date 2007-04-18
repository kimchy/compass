package org.compass.core.test.support.search;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
import org.compass.core.support.search.CompassSearchResults;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CompassSearchHelperTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"support/search/mapping.cpm.xml"};
    }

    public void testNoPagination() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 10);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass());
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test"));
        assertEquals(10, results.getHits().length);
        assertEquals(10, results.getTotalHits());

        tr.commit();
        session.close();
    }

    public void testNoPaginationWithCompassQuery() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 10);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass());
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand(session.queryBuilder().queryString("test").toQuery()));
        assertEquals(10, results.getHits().length);
        assertEquals(10, results.getTotalHits());

        tr.commit();
        session.close();
    }

    public void testSinglePage() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 10);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass(), new Integer(20));
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test"));
        assertEquals(10, results.getHits().length);
        assertEquals(10, results.getTotalHits());
        assertEquals(1, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(10, results.getPages()[0].getTo());
        assertEquals(10, results.getPages()[0].getSize());
        assertEquals(true, results.getPages()[0].isSelected());

        tr.commit();
        session.close();
    }

    public void testTwoPagesExactMatch() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 10);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass(), new Integer(5));
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test", new Integer(0)));
        assertEquals(5, results.getHits().length);
        assertEquals(10, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(true, results.getPages()[0].isSelected());
        assertEquals(6, results.getPages()[1].getFrom());
        assertEquals(10, results.getPages()[1].getTo());
        assertEquals(5, results.getPages()[1].getSize());
        assertEquals(false, results.getPages()[1].isSelected());

        searchHelper = new CompassSearchHelper(getCompass(), new Integer(5));
        results = searchHelper.search(new CompassSearchCommand("test", new Integer(1)));
        assertEquals(5, results.getHits().length);
        assertEquals(10, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(false, results.getPages()[0].isSelected());
        assertEquals(6, results.getPages()[1].getFrom());
        assertEquals(10, results.getPages()[1].getTo());
        assertEquals(5, results.getPages()[1].getSize());
        assertEquals(true, results.getPages()[1].isSelected());

        tr.commit();
        session.close();
    }

    public void testTwoPagesSmallerLastPage() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 8);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass(), new Integer(5));
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test", new Integer(0)));
        assertEquals(5, results.getHits().length);
        assertEquals(8, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(true, results.getPages()[0].isSelected());
        assertEquals(6, results.getPages()[1].getFrom());
        assertEquals(8, results.getPages()[1].getTo());
        assertEquals(3, results.getPages()[1].getSize());
        assertEquals(false, results.getPages()[1].isSelected());

        searchHelper = new CompassSearchHelper(getCompass(), new Integer(5));
        results = searchHelper.search(new CompassSearchCommand("test", new Integer(1)));
        assertEquals(3, results.getHits().length);
        assertEquals(8, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(false, results.getPages()[0].isSelected());
        assertEquals(6, results.getPages()[1].getFrom());
        assertEquals(8, results.getPages()[1].getTo());
        assertEquals(3, results.getPages()[1].getSize());
        assertEquals(true, results.getPages()[1].isSelected());

        tr.commit();
        session.close();
    }

    /**
     * The purpose of this test is to verify that the size of the second page is determined correctly.
     */
    public void testSecondSmallerPageSize() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 8);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass(), new Integer(5));
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test", new Integer(0)));
        assertEquals(5, results.getHits().length);
        assertEquals(8, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(true, results.getPages()[0].isSelected());
        assertEquals(6, results.getPages()[1].getFrom());
        assertEquals(8, results.getPages()[1].getTo());
        assertEquals(3, results.getPages()[1].getSize());
        assertEquals(false, results.getPages()[1].isSelected());
        
        searchHelper = new AbstractSearchHelper(getCompass(), new Integer(5));
        results = searchHelper.search(new CompassSearchCommand("test", new Integer(1)));
        assertEquals(3, results.getHits().length);
        assertEquals(8, results.getTotalHits());
        assertEquals(2, results.getPages().length);
        assertEquals(5,((AbstractSearchHelper)searchHelper).getExpectedFrom());
        assertEquals(3,((AbstractSearchHelper)searchHelper).getExpectedSize());
        
        tr.commit();
        session.close();
    }

    /**
     * The purpose is to test that if user asks for the second result page (even if
     * results fits the first page completely) that it returns the first page again
     * and no Exeption is thrown.
     */
    public void testSecondPageWhenFirstPageSmallerThenPageSize() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        addData(session, 5);
        CompassSearchHelper searchHelper = new CompassSearchHelper(getCompass(), new Integer(10));
        // Note that we are asking for the second result page even if results completely fits the first result page.
        CompassSearchResults results = searchHelper.search(new CompassSearchCommand("test", new Integer(1)));
        assertEquals(5, results.getHits().length);
        assertEquals(5, results.getTotalHits());
        assertEquals(1, results.getPages().length);
        assertEquals(1, results.getPages()[0].getFrom());
        assertEquals(5, results.getPages()[0].getTo());
        assertEquals(5, results.getPages()[0].getSize());
        assertEquals(true, results.getPages()[0].isSelected());

        tr.commit();
        session.close();
    }
    
    private void addData(CompassSession session, int size) {
        for (int i = 0; i < size; i++) {
            session.save(new A(i, "test value" + i));
        }
    }
}
