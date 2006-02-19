package org.compass.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author kimchy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableConstant {

    String name();

    String[] values();

    float boost() default 1.0f;

    Store store() default Store.YES;

    Index index() default Index.TOKENIZED;

    TermVector termVector() default TermVector.NO;

    String analyzer() default "";

    boolean exceludeFromAll() default false;

    boolean override() default true;

    /**
     * Converter for the Constant meta-data mapping.
     */
    String converter() default "";
}
