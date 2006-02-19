package org.compass.annotations.test.constant;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableConstantMetaDatas;
import org.compass.annotations.SearchableConstantMetaData;

/**
 * @author kimchy
 */
@Searchable
@SearchableConstantMetaDatas({
        @SearchableConstantMetaData(name = "const1", values = {"val11", "val12"}),
        @SearchableConstantMetaData(name = "const2", values = {"val21", "val22"})})
public class A {

    @SearchableId
    long id;
}
