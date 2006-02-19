package org.compass.annotations.test.converter;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.test.Converted;

/**
 * @author kimchy
 */
@Searchable
public class A {

    @SearchableId(converter = "myconv")
    Converted id;

    @SearchableProperty(converter = "myconv")
    Converted value;
}
