package org.compass.annotations.test.nounmarshall.component;

import java.util.List;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.SupportUnmarshall;

/**
 * @author kimchy
 */
@Searchable(supportUnmarshall = SupportUnmarshall.FALSE)
public class A {

    @SearchableId
    Integer id;

    @SearchableProperty(name = "value")
    String value;

    @SearchableComponent
    B b;

    @SearchableComponent
    List<B> bs;
}
