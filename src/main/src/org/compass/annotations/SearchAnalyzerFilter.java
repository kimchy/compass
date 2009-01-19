/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;

/**
 * Configures a {@link LuceneAnalyzerTokenFilterProvider} to be used within Compass.
 * Set on package definition (<code>package-info.java</code>).
 *
 * <p>The {@link LuceneAnalyzerTokenFilterProvider} is registed under a lookup
 * name ({@link #name()}), which can then be reference in in the analyzer definition
 * (i.e. {@link org.compass.annotations.SearchAnalyzer#filters()}).
 *
 * <p>Additional settings can be injected into the {@link LuceneAnalyzerTokenFilterProvider}
 * implementation using {@link #settings()}.
 *
 * @author kimchy
 * @see LuceneAnalyzerTokenFilterProvider
 * @see org.compass.core.lucene.engine.analyzer.synonym.SynonymAnalyzerTokenFilterProvider
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
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
