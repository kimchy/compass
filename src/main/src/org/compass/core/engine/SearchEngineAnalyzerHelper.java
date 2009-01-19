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

package org.compass.core.engine;

import java.io.Reader;

import org.compass.core.CompassToken;
import org.compass.core.Resource;

/**
 * @author kimchy
 */
public interface SearchEngineAnalyzerHelper {

    SearchEngineAnalyzerHelper setAnalyzer(String analyzerName) throws SearchEngineException;

    SearchEngineAnalyzerHelper setAnalyzer(Resource resource) throws SearchEngineException;

    SearchEngineAnalyzerHelper setAnalyzerByAlias(String alias) throws SearchEngineException;

    CompassToken analyzeSingle(String text) throws SearchEngineException;

    CompassToken[] analyze(String text) throws SearchEngineException;

    CompassToken[] analyze(String propertyName, String text) throws SearchEngineException;

    CompassToken[] analyze(Reader textReader) throws SearchEngineException;

    CompassToken[] analyze(String propertyName, Reader textReader) throws SearchEngineException;
}
