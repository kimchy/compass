
@SearchableConverter(name = "myconv", type = ConvertedConverter.class,
        settings = {@SearchableSetting(name = "separator", value = "#")})
package org.compass.annotations.test.converter;

import org.compass.annotations.SearchableConverter;
import org.compass.annotations.SearchableSetting;
import org.compass.annotations.test.ConvertedConverter;