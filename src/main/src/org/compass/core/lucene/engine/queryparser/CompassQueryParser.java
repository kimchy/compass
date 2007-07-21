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

package org.compass.core.lucene.engine.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.compass.core.lucene.search.ConstantScorePrefixQuery;
import org.compass.core.mapping.CompassMapping;

/**
 * Extends Lucene {@link org.apache.lucene.queryParser.QueryParser} and overrides {@link #getRangeQuery(String,String,String,boolean)}
 * since lucene performs data parsing which is a performance killer. Anyhow, handling dates in Compass
 * is different and simpler than Lucene.
 *
 * @author kimchy
 */
public class CompassQueryParser extends QueryParser {

    private CompassMapping mapping;

    public CompassQueryParser(String f, Analyzer a, CompassMapping mapping) {
        super(f, a);
        this.mapping = mapping;
    }

    /**
     * Override it so we won't use the date format to try and parse dates
     */
    protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException {
        if (getLowercaseExpandedTerms()) {
            part1 = part1.toLowerCase();
            part2 = part2.toLowerCase();
        }

        CompassMapping.ResourcePropertyLookup lookup = mapping.getResourcePropertyLookup(field);
        lookup.setConvertOnlyWithDotPath(false);
        if (lookup.hasSpecificConverter()) {
            if ("*".equals(part1)) {
                part1 = null;
            } else {
                part1 = lookup.getValue(lookup.fromString(part1));
            }
            if ("*".equals(part2)) {
                part2 = null;
            } else {
                part2 = lookup.getValue(lookup.fromString(part2));
            }
        } else {
            if ("*".equals(part1)) {
                part1 = null;
            }
            if ("*".equals(part2)) {
                part2 = null;
            }
        }

        return new ConstantScoreRangeQuery(field, part1, part2, inclusive, inclusive);
    }

    protected Query getPrefixQuery(String field, String termStr) throws ParseException {
        if (getLowercaseExpandedTerms()) {
            termStr = termStr.toLowerCase();
        }

        Term t = new Term(field, termStr);
        return new ConstantScorePrefixQuery(t);
    }
}
