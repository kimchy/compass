package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A dynamic meta data evaluation of the given expression using an expression
 * language library.
 *
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableDynamicMetaData {

    /**
     * The name of the meta-data. The name will be the value the contend will
     * be saved under, so it can later be searched using "author:london" (here
     * the name is author).
     */
    String name();

    /**
     * The expression to be evaluated by the dynamic expression evaluation library.
     */
    String expression();

    /**
     * The dynamic converter lookup name. Compass built in ones include: jexl, velocity.
     */
    String converter();

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
     * Specifies whether and how the meta-data proeprty should value will be revered.
     */
    Reverse reverse() default Reverse.NO;

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
     * @see org.compass.annotations.Searchable#enableAll
     */
    boolean excludeFromAll() default false;
    
    /**
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    String format() default "";

    /**
     * If using a format, the type of the expression result.
     */
    Class type() default Object.class;
}
