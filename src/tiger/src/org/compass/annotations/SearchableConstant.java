package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A constant meta-data that can be defined on a {@link Searchable} class.
 * <p/>
 * A constant meta-data is a predefined name and value pair that will be
 * saved in the search engine index.
 * <p/>
 * Multiple constants can be defined using the {@link SearchableConstants} annotation.
 *
 * @author kimchy
 * @see Searchable
 * @see SearchableConstants
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableConstant {

    /**
     * The name of the meta-data.
     */
    String name();

    /**
     * A list of values that the meta-data will have.
     */
    String[] values();

    /**
     * The boost level for the meta-data. Will cause hits
     * based on this meta-data to rank higher.
     */
    float boost() default 1.0f;

    /**
     * Specifies whether and how a meta-data property will be stored.
     */
    Store store() default Store.YES;

    /**
     * Specifies whether and how a meta-data proeprty should be indexed.
     */
    Index index() default Index.TOKENIZED;

    /**
     * Specifies whether and how a meta-data property should have term vectors.
     */
    TermVector termVector() default TermVector.NO;

    /**
     * Specifies a specialized analyzer lookup name that will be used to analyze
     * the meta-data content.
     * <p/>
     * Defaults to Compass default analyzer.
     */
    String analyzer() default "";

    /**
     * Specifies if this meta-data should be excluded from the generated
     * "all" meta-data.
     *
     * @see Searchable#enableAll
     */
    boolean excludeFromAll() default false;

    /**
     * Controls if the constant value should override the same constant defined
     * elsewhere for the same searchable class.
     */
    boolean override() default true;

    /**
     * Converter for the Constant meta-data mapping.
     */
    String converter() default "";
}
