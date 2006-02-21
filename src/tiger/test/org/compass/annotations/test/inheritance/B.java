package org.compass.annotations.test.inheritance;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class B extends A {

    @SearchableProperty(name = "value1e")
    String value1;

    @SearchableProperty(name = "value2e", override = false)
    String value2;
}
