package org.compass.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.lucene.analysis.Analyzer;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerFactory;

/**
 *
 * @author kimchy
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzer {

    String name();

    AnalyzerType type();

    SnowballType snowballType() default SnowballType.Porter;

    Class<? extends Analyzer> analyzerClass() default Analyzer.class;

    String[] filters() default {};

    Class<? extends LuceneAnalyzerFactory> factory() default LuceneAnalyzerFactory.class;

    /**
     * Only applies when using one of Compass internal analyzer types, and not the {@link AnalyzerType#ClassName}.
     */
    String[] stopWords() default {};

    /**
     * Only applies when using one of Compass internal analyzer types, and not the {@link AnalyzerType#ClassName}.
     */
    boolean addStopWords() default true;

    SearchSetting[] settings() default {};
}
