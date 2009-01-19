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

package org.compass.core.impl;

import org.compass.core.CompassException;
import org.compass.core.CompassHighlighter;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.spi.InternalCompassHits;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class DefaultCompassHighlighter implements CompassHighlighter {

    private Resource resource;

    private SearchEngineHighlighter highlighter;

    private InternalCompassSession session;

    private InternalCompassHits hits;

    private int hitNumber;

    public DefaultCompassHighlighter(InternalCompassSession session, InternalCompassHits hits, int n) {
        this.session = session;
        this.hits = hits;
        this.hitNumber = n;
        this.highlighter = hits.getSearchEngineHits().getHighlighter();
        this.resource = hits.resource(n);
        setAnalyzer(resource);
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
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        String fragment = highlighter.fragment(resource, lookup.getPath());
        hits.setHighlightedText(hitNumber, propertyName, fragment);
        return fragment;
    }

    public String fragment(String propertyName, String text) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        String fragment = highlighter.fragment(resource, lookup.getPath(), text);
        hits.setHighlightedText(hitNumber, propertyName, fragment);
        return fragment;
    }

    public String[] fragments(String propertyName) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragments(resource, lookup.getPath());
    }

    public String[] fragments(String propertyName, String text) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return highlighter.fragments(resource, lookup.getPath(), text);
    }

    public String fragmentsWithSeparator(String propertyName) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        String fragment = highlighter.fragmentsWithSeparator(resource, lookup.getPath());
        hits.setHighlightedText(hitNumber, propertyName, fragment);
        return fragment;
    }

    public String fragmentsWithSeparator(String propertyName, String text) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        String fragment = highlighter.fragmentsWithSeparator(resource, lookup.getPath(), text);
        hits.setHighlightedText(hitNumber, propertyName, fragment);
        return fragment;
    }

	public String[] multiValueFragment(String propertyName) throws CompassException {
		ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
		String[] fragments = highlighter.multiValueFragment(resource, lookup.getPath());
		return fragments;
	}

	public String[] multiValueFragment(String propertyName, String[] texts) throws CompassException {
		ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
		String[] fragments = highlighter.multiValueFragment(resource, lookup.getPath(), texts);
		return fragments;
	}

	public String multiValueFragmentWithSeparator(String propertyName) throws CompassException {
		ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
		String fragment = highlighter.multiValueFragmentWithSeparator(resource, lookup.getPath());
		hits.setHighlightedText(hitNumber, propertyName, fragment);
		return fragment;
	}

	public String multiValueFragmentWithSeparator(String propertyName, String[] texts)
			throws CompassException {
		ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
		String fragment = highlighter.multiValueFragmentWithSeparator(resource, lookup.getPath(), texts);
		hits.setHighlightedText(hitNumber, propertyName, fragment);
		return fragment;
	}
}
