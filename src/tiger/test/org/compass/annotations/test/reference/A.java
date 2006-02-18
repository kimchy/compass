package org.compass.annotations.test.reference;

import java.util.List;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.SearchableReference;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    long id;

    @SearchableProperty
    String value;

    @SearchableReference
    B b;

    @SearchableReference
    List<B> bValues;
}
