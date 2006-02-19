package org.compass.annotations.test.id;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.test.Converted;

/**
 * @author kimchy
 */
@Searchable
public class B {

    @SearchableId
    Converted id;
}
