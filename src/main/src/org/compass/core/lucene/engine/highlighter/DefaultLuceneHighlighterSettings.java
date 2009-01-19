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

package org.compass.core.lucene.engine.highlighter;

import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.compass.core.CompassHighlighter;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class DefaultLuceneHighlighterSettings implements LuceneHighlighterSettings {

    private CompassSettings settings;

    private Formatter formatter;

    private String fragmenterSetting;

    private Encoder encoder;
    
    private boolean rewriteQuery = true;
    
    private boolean computeIdf = true;
    
    private int maxNumFragments;
    
    private String separator;
    
    private int maxBytesToAnalyze;
    
    private CompassHighlighter.TextTokenizer textTokenizer;

    public DefaultLuceneHighlighterSettings(Formatter formatter, String fragmenterSetting, Encoder encoder) {
        this.formatter = formatter;
        this.fragmenterSetting = fragmenterSetting;
        this.encoder = encoder;
    }

    public void configure(CompassSettings settings) throws SearchEngineException {
        this.settings = settings;
    }

    public Encoder getEncoder() throws SearchEngineException {
        return encoder;
    }

    public Formatter getFormatter() throws SearchEngineException {
        return formatter;
    }

    public Fragmenter getFragmenter() throws SearchEngineException {
        if (fragmenterSetting == null || fragmenterSetting.equals(LuceneEnvironment.Highlighter.Fragmenter.TYPE_SIMPLE)) {
            int size = settings.getSettingAsInt(LuceneEnvironment.Highlighter.Fragmenter.SIMPLE_SIZE, 100);
            return new SimpleFragmenter(size);
        }
        Fragmenter oFragmenter;
        if (fragmenterSetting.equals(LuceneEnvironment.Highlighter.Fragmenter.TYPE_NULL)) {
            oFragmenter = new NullFragmenter();
        } else {
            try {
                Class fragmenterClass = ClassUtils.forName(fragmenterSetting, settings.getClassLoader());
                oFragmenter = (Fragmenter) fragmenterClass.newInstance();
            } catch (Exception e) {
                throw new SearchEngineException("Failed to create highlighter fragmenter class [" + fragmenterSetting
                        + "]", e);
            }
            if (oFragmenter instanceof CompassConfigurable) {
                ((CompassConfigurable) oFragmenter).configure(settings);
            }
        }
        return oFragmenter;
    }

    public boolean isRewriteQuery() {
        return rewriteQuery;
    }

    public void setRewriteQuery(boolean rewriteQuery) {
        this.rewriteQuery = rewriteQuery;
    }

    public boolean isComputeIdf() {
        return computeIdf;
    }

    public void setComputeIdf(boolean computeIdf) {
        this.computeIdf = computeIdf;
    }

    public int getMaxNumFragments() {
        return maxNumFragments;
    }

    public void setMaxNumFragments(int maxNumFragments) {
        this.maxNumFragments = maxNumFragments;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int getMaxBytesToAnalyze() {
        return maxBytesToAnalyze;
    }

    public void setMaxBytesToAnalyze(int maxBytesToAnalyze) {
        this.maxBytesToAnalyze = maxBytesToAnalyze;
    }

    public CompassHighlighter.TextTokenizer getTextTokenizer() {
        return textTokenizer;
    }

    public void setTextTokenizer(CompassHighlighter.TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
    }
}
