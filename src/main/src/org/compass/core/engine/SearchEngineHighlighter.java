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

import org.compass.core.CompassHighlighter;
import org.compass.core.Resource;

/**
 * 
 * @author kimchy
 * 
 */
public interface SearchEngineHighlighter {

    SearchEngineHighlighter clear();
    
    void close() throws SearchEngineException;
    
    SearchEngineHighlighter setHighlighter(String highlighterName) throws SearchEngineException;

    SearchEngineHighlighter setAnalyzer(String analyzerName) throws SearchEngineException;

    SearchEngineHighlighter setAnalyzer(Resource resource) throws SearchEngineException;

    SearchEngineHighlighter setSeparator(String separator) throws SearchEngineException;

    SearchEngineHighlighter setMaxNumFragments(int maxNumFragments) throws SearchEngineException;

    SearchEngineHighlighter setMaxBytesToAnalyze(int maxBytesToAnalyze) throws SearchEngineException;
    
    SearchEngineHighlighter setTextTokenizer(CompassHighlighter.TextTokenizer textTokenizer)
            throws SearchEngineException;

    String fragment(Resource resource, String propertyName) throws SearchEngineException;

    String fragment(Resource resource, String propertyName, String text) throws SearchEngineException;

    String[] fragments(Resource resource, String propertyName) throws SearchEngineException;

    String[] fragments(Resource resource, String propertyName, String text) throws SearchEngineException;

    String fragmentsWithSeparator(Resource resource, String propertyName) throws SearchEngineException;

    String fragmentsWithSeparator(Resource resource, String propertyName, String text) throws SearchEngineException;
    
    String[] multiValueFragment(Resource resource, String propertyName) throws SearchEngineException;
    
    String[] multiValueFragment(Resource resource, String propertyName, String[] texts) throws SearchEngineException;
    
    String multiValueFragmentWithSeparator(Resource resource, String propertyName) throws SearchEngineException;
    
    String multiValueFragmentWithSeparator(Resource resource, String propertyName, String[] texts) throws SearchEngineException;
}
