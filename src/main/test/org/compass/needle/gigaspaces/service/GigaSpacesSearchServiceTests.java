package org.compass.needle.gigaspaces.service;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class GigaSpacesSearchServiceTests extends AbstractDependencyInjectionSpringContextTests {

    protected CompassSearchService clientSearchService;

    protected GigaSpace clusteredGigaSpace;

    public GigaSpacesSearchServiceTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"org/compass/needle/gigaspaces/service/context.xml"};
    }

    public void testSearchService() throws Exception {
        assertEquals(0, clientSearchService.search("test").getTotalLength());

        A a = new A();
        a.value = "test";
        a.id = "1";
        clusteredGigaSpace.write(a);

        Thread.sleep(1000);

        SearchResults results = clientSearchService.search("test");
        assertEquals(1, results.getTotalLength());
        assertEquals("test", ((A) results.getResults()[0].getData()).getValue());

        results = clientSearchService.search("noway");
        assertEquals(0, results.getTotalLength());
        assertEquals(0, results.getResults().length);

        SearchResourceResults resourceResults = clientSearchService.searchResource("test");
        assertEquals(1, resourceResults.getTotalLength());
        assertEquals("test", resourceResults.getResults()[0].getResource().getValue("value"));

        resourceResults = clientSearchService.searchResource("noway");
        assertEquals(0, resourceResults.getTotalLength());
        assertEquals(0, resourceResults.getResults().length);

        a = new A();
        a.value = "best";
        a.id = "2";
        clusteredGigaSpace.write(a);

        Thread.sleep(1000);

        results = clientSearchService.search("test~0.5");
        assertEquals(2, results.getTotalLength());

        resourceResults = clientSearchService.searchResource("test~0.5");
        assertEquals(2, resourceResults.getTotalLength());

        results = clientSearchService.search("test~0.5", 1);
        assertEquals(2, results.getTotalLength());
        assertEquals(1, results.getResults().length);

        resourceResults = clientSearchService.searchResource("test~0.5", 1);
        assertEquals(2, resourceResults.getTotalLength());
        assertEquals(1, resourceResults.getResults().length);

        results = clientSearchService.search("test~0.5", 10);
        assertEquals(2, results.getTotalLength());
        assertEquals(2, results.getResults().length);

        results = clientSearchService.search("test~0.5", 1);
        assertEquals(2, results.getTotalLength());
        assertEquals(1, results.getResults().length);
        assertEquals("test", ((A) results.getResults()[0].getData()).getValue());

        results = clientSearchService.search("test~0.5", 1, results.getLowestScore());
        assertEquals(2, results.getTotalLength());
        assertEquals(1, results.getResults().length);
        assertEquals("best", ((A) results.getResults()[0].getData()).getValue());

        resourceResults = clientSearchService.searchResource("test~0.5", 1);
        assertEquals(2, resourceResults.getTotalLength());
        assertEquals(1, resourceResults.getResults().length);
        assertEquals("test", resourceResults.getResults()[0].getResource().getValue("value"));

        resourceResults = clientSearchService.searchResource("test~0.5", 1, resourceResults.getLowestScore());
        assertEquals(2, resourceResults.getTotalLength());
        assertEquals(1, resourceResults.getResults().length);
        assertEquals("best", resourceResults.getResults()[0].getResource().getValue("value"));
    }
}
