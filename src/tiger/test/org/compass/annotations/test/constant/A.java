package org.compass.annotations.test.constant;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableConstants;
import org.compass.annotations.SearchableConstant;

/**
 * @author kimchy
 */
@Searchable
@SearchableConstants({
        @SearchableConstant(name = "const1", values = {"val11", "val12"}),
        @SearchableConstant(name = "const2", values = {"val21", "val22"})})
public class A {

    @SearchableId
    long id;
}
