package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a collection of {@link SearchAnalyzer}s.
 * Set on package definition (<code>package-info.java</code>).
 *
 * @author kimchy
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzers {

    /**
     * A collection of analyzers to confiugre.
     */
    SearchAnalyzer[] value();
}
