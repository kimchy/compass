package org.compass.annotations.test.simple;

import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
public interface CInterface {

    @SearchableId
    int getId();

    @SearchableProperty
    String getValue();
}
