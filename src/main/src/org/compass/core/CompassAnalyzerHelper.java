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

package org.compass.core;

import java.io.Reader;

/**
 * A set of Analyzer related helper methods. Aimed to help understand and simulate
 * the analysis process. As well as helping build advance query building capabilities.
 *
 * @author kimchy
 */
public interface CompassAnalyzerHelper {

    /**
     * Sets the analyzer that will be used for the analysis of the text.
     *
     * @param analyzerName The analyzer name that will be used.
     * @return the analyzer helper
     * @throws CompassException
     */
    CompassAnalyzerHelper setAnalyzer(String analyzerName) throws CompassException;

    /**
     * Sets the analyzer that will be used for the analysis of the text.
     * Uses the resource to derive the analyzer
     * that will be used (works also with per resource property analyzer).
     *
     * @param resource The resource to derive the analyzer from
     * @return the analyzer helper
     * @throws CompassException
     */
    CompassAnalyzerHelper setAnalyzer(Resource resource) throws CompassException;

    /**
     * Sets the analyzer that will be used for the analysis of the text.
     * Uses the alias to get the mapping deinfitions and build a specific analyzer
     * if there is a certain property that is associated with a specific analyzer
     * (builds a per resource property analyzer).
     *
     * @param alias The alias to derive the analyzer from
     * @return the analyzer helper
     * @throws CompassException If the analyzer if not found
     */
    CompassAnalyzerHelper setAnalyzerByAlias(String alias) throws CompassException;

    /**
     * Analyzes the given text, returning the first token.
     *
     * @param text The text to analyze
     * @return The first token.
     * @throws CompassException
     */
    CompassToken analyzeSingle(String text) throws CompassException;

    /**
     * Analyzes the given text, returning a set of tokens.
     *
     * @param text The text to analyze
     * @return A set of tokens resulting from the analysis process.
     * @throws CompassException
     */
    CompassToken[] analyze(String text) throws CompassException;

    /**
     * Analyzes the given text, using (if needed) the anlayzer that is bound
     * to the supplied property. Should be used with {@link #setAnalyzer(Resource)}
     * so the analyzer can be dynamically detected from the resource.
     *
     * @param propertyName The property name for analyze bound properties
     * @param text The text to analyze
     * @return A set of tokens resulting from the analysis process.
     * @throws CompassException
     */
    CompassToken[] analyze(String propertyName, String text) throws CompassException;

    /**
     * Analyzes the given text, returning a set of tokens.
     *
     * @param textReader The text to analyze
     * @return A set of tokens resulting from the analysis process.
     * @throws CompassException
     */
    CompassToken[] analyze(Reader textReader) throws CompassException;

    /**
     * Analyzes the given text, using (if needed) the anlayzer that is bound
     * to the supplied property. Should be used with {@link #setAnalyzer(Resource)}
     * so the analyzer can be dynamically detected from the resource.
     *
     * @param propertyName The property name for analyze bound properties
     * @param textReader The text to analyze
     * @return A set of tokens resulting from the analysis process.
     * @throws CompassException
     */
    CompassToken[] analyze(String propertyName, Reader textReader) throws CompassException;

}
