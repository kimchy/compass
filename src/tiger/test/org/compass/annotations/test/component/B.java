package org.compass.annotations.test.component;

import org.compass.annotations.ManagedIdIndex;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

/**
 * @author kimchy
 */
@Searchable(root = false)
public class B {

    public B() {

    }

    public B(String value) {
        this.value = value;
    }

    @SearchableProperty(name = "bValue", managedIdIndex = ManagedIdIndex.UN_TOKENIZED)
    String value;
}
