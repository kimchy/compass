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

import java.io.Reader;

import org.compass.core.CompassAnalyzerHelper;
import org.compass.core.CompassException;
import org.compass.core.CompassToken;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class DefaultCompassAnalyzerHelper implements CompassAnalyzerHelper {

    private InternalCompassSession session;

    private SearchEngineAnalyzerHelper analyzerHelper;

    public DefaultCompassAnalyzerHelper(SearchEngineAnalyzerHelper analyzerHelper, InternalCompassSession session) {
        this.session = session;
        this.analyzerHelper = analyzerHelper;
    }

    public CompassAnalyzerHelper setAnalyzer(String analyzerName) throws CompassException {
        analyzerHelper.setAnalyzer(analyzerName);
        return this;
    }

    public CompassAnalyzerHelper setAnalyzer(Resource resource) throws CompassException {
        analyzerHelper.setAnalyzer(resource);
        return this;
    }

    public CompassAnalyzerHelper setAnalyzerByAlias(String alias) throws CompassException {
        analyzerHelper.setAnalyzerByAlias(alias);
        return this;
    }

    public CompassToken analyzeSingle(String text) throws CompassException {
        return analyzerHelper.analyzeSingle(text);
    }

    public CompassToken[] analyze(String text) throws CompassException {
        return analyzerHelper.analyze(text);
    }

    public CompassToken[] analyze(String propertyName, String text) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return analyzerHelper.analyze(lookup.getPath(), text);
    }

    public CompassToken[] analyze(Reader textReader) throws CompassException {
        return analyzerHelper.analyze(textReader);
    }

    public CompassToken[] analyze(String propertyName, Reader textReader) throws CompassException {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(propertyName);
        return analyzerHelper.analyze(lookup.getPath(), textReader);
    }
}
