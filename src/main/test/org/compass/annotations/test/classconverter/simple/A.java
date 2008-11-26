package org.compass.annotations.test.classconverter.simple;

import org.compass.annotations.ExcludeFromAll;
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

    @SearchableProperty(excludeFromAll = ExcludeFromAll.YES)
    ZipCode zipcode;

    @SearchableProperty(excludeFromAll = ExcludeFromAll.NO)
    ZipCode zipcode2;
}
