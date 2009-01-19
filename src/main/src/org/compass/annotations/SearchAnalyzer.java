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

import org.apache.lucene.analysis.Analyzer;

/**
 * Configure {@link Analyzer} to be used within Compass.
 * Set on package definition (<code>package-info.java</code>).
 *
 * <p>The {@link Analyzer} is registed under a lookup name ({@link #name()}), which can then
 * be reference in the different mapping definitions.
 *
 * <p>Allows for simple configuration of all analyzers that come with Compass using {@link #type()}.
 * If the {@link #type()} is set to {@link AnalyzerType#Snowball}, the {@link #snowballType()}
 * can be used to further configure the snowball analyzer. If a custom converter needs to be
 * registered with Compass, the {@link AnalyzerType#CustomAnalyzer} needs to be set on {@link #type()},
 * and the {@link #analyzerClass()} needs to be configured with the class that implements it.
 *
 * <p>A set of stop words can be added/replace the stop words the internal analyzers are configured
 * with. The stop words will be added if the {@link #addStopWords()} is set to <code>true</code>.
 *
 * <p>Further settings can be set for a specialized analyzer using {@link #settings()}. If the
 * specialized {@link Analyzer} requires settings to be injected, it needs to implement the
 * {@link org.compass.core.config.CompassConfigurable} interface.
 *
 * <p>To replace Compas default analyzer, the {@link #name()} should be set
 * {@link org.compass.core.lucene.LuceneEnvironment.Analyzer#DEFAULT_GROUP}.
 *
 * <p>To replace Compass search analyzer (which defaults to the default analyzer if not set), the
 * {@link #name()} should be set to {@link org.compass.core.lucene.LuceneEnvironment.Analyzer#SEARCH_GROUP}.
 *
 * <p>Multiple analyzers can be defined using the {@link SearchAnalyzers} annotation.
 *
 * <p>Note, that Analyzers can also be conifugred using other Compass configuration mechanism.
 *
 * @author kimchy
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchAnalyzer {

    /**
     * The name the analyzer will be registered under.
     */
    String name();

    /**
     * The type of the analyzer. For custom {@link Analyzer} implementation
     * the {@link AnalyzerType#CustomAnalyzer} should be set, and the {@link #analyzerClass()}
     * should have the custom {@link Analyzer} class set.
     */
    AnalyzerType type();

    /**
     * If the {@link #type()} is set to {@link AnalyzerType#Snowball}, controls
     * the snowball analyzer type.
     */
    SnowballType snowballType() default SnowballType.Porter;

    /**
     * The custom {@link Analyzer} implementation. Used when the {@link #type()}
     * is set to {@link AnalyzerType#CustomAnalyzer}.
     */
    Class<? extends Analyzer> analyzerClass() default Analyzer.class;

    /**
     * A set of {@link org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider}s
     * lookup names to be used with the {@link Analyzer}.
     *
     * <p>Filters can be configured using {@link SearchAnalyzerFilter} or using Compass configuration.
     */
    String[] filters() default {};

    /**
     * A set of stop words that will be added/replace the stop words that comes with Compass intenral
     * analyzers.
     *
     * <p>Only applies when using one of Compass internal analyzer types, and not the {@link AnalyzerType#CustomAnalyzer}.
     */
    String[] stopWords() default {};

    /**
     * Add the set of {@link #stopWords()} to the default set of stop words if set to <code>true</code>.
     * Replaces them if set to <code>false</code>.
     * <p/>
     * Only applies when using one of Compass internal analyzer types, and not the {@link AnalyzerType#CustomAnalyzer}.
     */
    boolean addStopWords() default true;

    /**
     * Further settings for a custom {@link Analyzer} implementation.
     */
    SearchSetting[] settings() default {};
}
