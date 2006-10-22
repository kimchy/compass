package org.compass.annotations.test.inheritance;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;

/**
 * @author kimchy
 */
@Searchable
public class C {

    @SearchableId
    int id;

    @SearchableComponent
    A a;
}
