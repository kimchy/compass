package org.compass.annotations;

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author kimchy
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzerFilter {

    String name();

    Class<? extends LuceneAnalyzerTokenFilterProvider> type();

    SearchSetting[] settings() default {};
}
