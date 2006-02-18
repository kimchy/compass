package org.compass.annotations.test.reference;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class B {

    @SearchableId
    long id;

    @SearchableProperty
    String value;
}
