package org.compass.annotations.test;

import java.io.Serializable;

import org.compass.annotations.SearchableClassConverter;

/**
 * @author kimchy
 */
@SearchableClassConverter(ConverterdConverter.class)
public class Converted implements Serializable {

    public Converted() {

    }

    public Converted(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public String value1;

    public String value2;

}
