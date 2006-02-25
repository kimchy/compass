package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;

/**
 * Configures a {@link LuceneAnalyzerTokenFilterProvider} to be used within Compass.
 * Set on package definition (<code>package-info.java</code>).
 * <p/>
 * The {@link LuceneAnalyzerTokenFilterProvider} is registed under a lookup
 * name ({@link #name()}), which can then be reference in in the analyzer definition
 * (i.e. {@link org.compass.annotations.SearchAnalyzer#filters()}).
 * <p/>
 * Additional settings can be injected into the {@link LuceneAnalyzerTokenFilterProvider}
 * implementation using {@link #settings()}.
 *
 * @author kimchy
 * @see LuceneAnalyzerTokenFilterProvider
 * @see org.compass.core.lucene.engine.analyzer.synonym.SynonymAnalyzerTokenFilterProvider
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzerFilter {

    /**
     * The name the analyzer token filter provider will be registered under.
     */
    String name();

    /**
     * The {@link LuceneAnalyzerTokenFilterProvider} implementation.
     */
    Class<? extends LuceneAnalyzerTokenFilterProvider> type();

    /**
     * Additional settings for the {@link LuceneAnalyzerTokenFilterProvider} implementation.
     */
    SearchSetting[] settings() default {};
}
