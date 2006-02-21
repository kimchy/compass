package org.compass.annotations.test.inheritance;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    int id;

    @SearchableProperty(name = "value1")
    String value1;

    @SearchableProperty(name = "value2")
    String value2;
}
