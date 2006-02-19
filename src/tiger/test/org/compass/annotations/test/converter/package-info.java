
@SearchableConverter(name = "myconv", type = ConvertedConverter.class,
        settings = {@SearchableConverterSetting(name = "separator", value = "#")})
package org.compass.annotations.test.converter;

import org.compass.annotations.SearchableConverter;
import org.compass.annotations.SearchableConverterSetting;
import org.compass.annotations.test.ConvertedConverter;