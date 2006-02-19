package org.compass.annotations.test.analyzer;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableAnalyzer;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId
    int id;

    @SearchableAnalyzer
    String analyzer;

    @SearchableProperty
    String value;
}
