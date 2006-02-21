package org.compass.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.compass.core.converter.Converter;

/**
 *
 * @author kimchy
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchConverter {

    String name();

    Class<? extends Converter> type();

    SearchSetting[] settings() default {};
}
