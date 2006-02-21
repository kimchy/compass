package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kimchy
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzerFilters {

    SearchAnalyzerFilter[] value();
}
