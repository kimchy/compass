package org.compass.annotations.test.nounmarshall.component;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable(root = false)
public class B {

    @SearchableProperty(name = "value")
    String value;

    @SearchableProperty(name = "value")
    String value2;
}
