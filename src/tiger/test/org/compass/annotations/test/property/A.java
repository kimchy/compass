package org.compass.annotations.test.property;

import java.util.List;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    long id;

    @SearchableProperty
    List<String> values;
}
