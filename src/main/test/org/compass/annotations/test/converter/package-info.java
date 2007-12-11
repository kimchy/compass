
@SearchConverter(name = "myconv", type = ConvertedConverter.class,
        settings = {@SearchSetting(name = "separator", value = "#")})
package org.compass.annotations.test.converter;

import org.compass.annotations.SearchConverter;
import org.compass.annotations.SearchSetting;
import org.compass.annotations.test.ConvertedConverter;