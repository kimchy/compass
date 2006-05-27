/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.impl;

import org.compass.core.CompassException;
import org.compass.core.CompassHighlighter;
import org.compass.core.Resource;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.mapping.CompassMapping;

/**
 * 
 * @author kimchy
 * 
 */
public class DefaultCompassHighlighter implements CompassHighlighter {

    private Resource resource;

    private SearchEngineHighlighter highlighter;

    private InternalCompassSession session;

    public DefaultCompassHighlighter(InternalCompassSession session, SearchEngineHighlighter highlighter, Resource resource) {
        this.session = session;
        this.highlighter = highlighter;
        this.resource = resource;
    }

    public CompassHighlighter setAnalyzer(String analyzerName) throws CompassException {
        highlighter.setAnalyzer(analyzerName);
        return this;
    }

    public CompassHighlighter setAnalyzer(Resource resource) throws CompassException {
        highlighter.setAnalyzer(resource);
        return this;
    }

    public CompassHighlighter setHighlighter(String highlighterName) throws CompassException {
        highlighter.setHighlighter(highlighterName);
        return this;
    }

    public CompassHighlighter setSeparator(String separator) throws CompassException {
        highlighter.setSeparator(separator);
        return this;
    }

    public CompassHighlighter setMaxNumFragments(int maxNumFragments) throws CompassException {
        highlighter.setMaxNumFragments(maxNumFragments);
        return this;
    }
    
    public CompassHighlighter setMaxBytesToAnalyze(int maxBytesToAnalyze) throws CompassException {
        highlighter.setMaxBytesToAnalyze(maxBytesToAnalyze);
        return this;
    }

    public CompassHighlighter setTextTokenizer(TextTokenizer textTokenizer) throws CompassException {
        highlighter.setTextTokenizer(textTokenizer);
        return this;
    }

    public String fragment(String propertyName) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragment(resource, lookup.getPath());
    }

    public String fragment(String propertyName, String text) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragment(resource, lookup.getPath(), text);
    }

    public String[] fragments(String propertyName) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragments(resource, lookup.getPath());
    }

    public String[] fragments(String propertyName, String text) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragments(resource, lookup.getPath(), text);
    }

    public String fragmentsWithSeparator(String propertyName) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragmentsWithSeparator(resource, lookup.getPath());
    }

    public String fragmentsWithSeparator(String propertyName, String text) throws CompassException {
        CompassMapping.ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragmentsWithSeparator(resource, lookup.getPath(), text);
    }
}
