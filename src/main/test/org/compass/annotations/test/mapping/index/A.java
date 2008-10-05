package org.compass.annotations.test.mapping.index;

import org.compass.annotations.Index;
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

    @SearchableProperty(index = Index.NOT_ANALYZED)
    String untokenized;

    @SearchableProperty(index = Index.ANALYZED)
    String tokenized;

    @SearchableProperty(index = Index.NO)
    String no;
}
