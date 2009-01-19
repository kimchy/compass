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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SpanGradientFormatter;
import org.compass.core.CompassHighlighter;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class DefaultLuceneHighlighterFactory implements LuceneHighlighterFactory {

    private static final Log log = LogFactory.getLog(DefaultLuceneHighlighterFactory.class);

    public LuceneHighlighterSettings createHighlighterSettings(String highlighterName, CompassSettings settings)
            throws SearchEngineException {

        Formatter formatter = createFormatter(highlighterName, settings);
        Encoder encoder = createEncoder(highlighterName, settings);
        String fragmenterSetting = settings.getSetting(LuceneEnvironment.Highlighter.Fragmenter.TYPE, null);

        boolean shouldRewriteQuery = settings.getSettingAsBoolean(LuceneEnvironment.Highlighter.REWRITE_QUERY, true);

        boolean computeIdf;
        String computeIdfSetting = settings.getSetting(LuceneEnvironment.Highlighter.COMPUTE_IDF);
        if (computeIdfSetting == null) {
            computeIdf = formatterRequiresToComputeIdf(formatter);
        } else {
            computeIdf = Boolean.valueOf(computeIdfSetting);
        }

        int maxNumFragments = settings.getSettingAsInt(LuceneEnvironment.Highlighter.MAX_NUM_FRAGMENTS, 3);
        String separator = settings.getSetting(LuceneEnvironment.Highlighter.SEPARATOR, "...");
        int maxBytesToAnalyze = settings.getSettingAsInt(LuceneEnvironment.Highlighter.MAX_BYTES_TO_ANALYZE, 50 * 1024);
        String textTokenizerSetting = settings.getSetting(LuceneEnvironment.Highlighter.TEXT_TOKENIZER,
                CompassHighlighter.TextTokenizer.toString(CompassHighlighter.TextTokenizer.AUTO));
        CompassHighlighter.TextTokenizer textTokenizer = CompassHighlighter.TextTokenizer
                .fromString(textTokenizerSetting);

        DefaultLuceneHighlighterSettings highlighterSettings = new DefaultLuceneHighlighterSettings(formatter,
                fragmenterSetting, encoder);
        highlighterSettings.setComputeIdf(computeIdf);
        highlighterSettings.setRewriteQuery(shouldRewriteQuery);
        highlighterSettings.configure(settings);
        highlighterSettings.setMaxNumFragments(maxNumFragments);
        highlighterSettings.setSeparator(separator);
        highlighterSettings.setMaxBytesToAnalyze(maxBytesToAnalyze);
        highlighterSettings.setTextTokenizer(textTokenizer);

        return highlighterSettings;
    }

    protected boolean formatterRequiresToComputeIdf(Formatter formatter) {
        return formatter instanceof SpanGradientFormatter;
    }

    protected Encoder createEncoder(String highlighterName, CompassSettings settings) throws SearchEngineException {
        Encoder encoder;
        Object obj = settings.getSetting(LuceneEnvironment.Highlighter.Encoder.TYPE);
        if (obj instanceof Encoder) {
            encoder = (Encoder) obj;
            if (log.isDebugEnabled()) {
                log.debug("Highlighter [" + highlighterName + "] uses encoder instance [" + encoder + "]");
            }
        } else {
            String encoderSetting = settings.getSetting(LuceneEnvironment.Highlighter.Encoder.TYPE,
                    LuceneEnvironment.Highlighter.Encoder.DEFAULT);
            if (log.isDebugEnabled()) {
                log.debug("Highlighter [" + highlighterName + "] uses encoder [" + encoderSetting + "]");
            }
            if (LuceneEnvironment.Highlighter.Encoder.DEFAULT.equals(encoderSetting)) {
                encoder = new DefaultEncoder();
            } else if (LuceneEnvironment.Highlighter.Encoder.HTML.equals(encoderSetting)) {
                encoder = new SimpleHTMLEncoder();
            } else {
                try {
                    // the formatter is the fully qualified class name
                    encoder = (Encoder) ClassUtils.forName(encoderSetting, settings.getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new SearchEngineException("Cannot instantiate Lucene encoder [" + encoderSetting
                            + "] for highlighter [" + highlighterName
                            + "]. Please verify the highlighter encoder setting at ["
                            + LuceneEnvironment.Highlighter.Encoder.TYPE + "]", e);
                }
            }
        }
        if (encoder instanceof CompassConfigurable) {
            ((CompassConfigurable) encoder).configure(settings);
        }
        return encoder;
    }

    protected Formatter createFormatter(String highlighterName, CompassSettings settings) throws SearchEngineException {
        Formatter formatter;
        Object obj = settings.getSettingAsObject(LuceneEnvironment.Highlighter.Formatter.TYPE);
        if (obj instanceof Formatter) {
            formatter = (Formatter) obj;
            if (log.isDebugEnabled()) {
                log.debug("Highlighter [" + highlighterName + "] uses formatter instance [" + formatter + "]");
            }
        } else {
            String formatterSettings = settings.getSetting(LuceneEnvironment.Highlighter.Formatter.TYPE,
                    LuceneEnvironment.Highlighter.Formatter.SIMPLE);
            if (log.isDebugEnabled()) {
                log.debug("Highlighter [" + highlighterName + "] uses formatter [" + formatterSettings + "]");
            }
            if (LuceneEnvironment.Highlighter.Formatter.SIMPLE.equals(formatterSettings)) {
                String preTag = settings.getSetting(LuceneEnvironment.Highlighter.Formatter.SIMPLE_PRE_HIGHLIGHT, "<b>");
                String postTag = settings.getSetting(LuceneEnvironment.Highlighter.Formatter.SIMPLE_POST_HIGHLIGHT, "</b>");
                formatter = new SimpleHTMLFormatter(preTag, postTag);
                if (log.isDebugEnabled()) {
                    log.debug("Highlighter [" + highlighterName + "] uses pre [" + preTag + "] and post [" + postTag + "]");
                }
            } else if (LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT.equals(formatterSettings)) {
                float maxScore = settings.getSettingAsFloat(
                        LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_SCORE, Float.MIN_VALUE);
                if (maxScore == Float.MIN_VALUE) {
                    throw new SearchEngineException("Highlighter [" + highlighterName
                            + "] uses span formatter and must set the ["
                            + LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_SCORE + "] setting");
                }
                String minForegroundColor = settings
                        .getSetting(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MIN_FOREGROUND_COLOR);
                String maxForegroundColor = settings
                        .getSetting(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_FOREGROUND_COLOR);
                String minBackgroundColor = settings
                        .getSetting(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MIN_BACKGROUND_COLOR);
                String maxBackgroundColor = settings
                        .getSetting(LuceneEnvironment.Highlighter.Formatter.HTML_SPAN_GRADIENT_MAX_BACKGROUND_COLOR);
                try {
                    formatter = new SpanGradientFormatter(maxScore, minForegroundColor, maxForegroundColor,
                            minBackgroundColor, maxBackgroundColor);
                } catch (IllegalArgumentException e) {
                    throw new SearchEngineException("Highlighter [" + highlighterName
                            + "] using span gradient formatter failed [" + e.getMessage() + "]");
                }
            } else {
                try {
                    // the formatter is the fully qualified class name
                    formatter = (Formatter) ClassUtils.forName(formatterSettings, settings.getClassLoader()).newInstance();
                } catch (Exception e) {
                    throw new SearchEngineException("Cannot instantiate Lucene formatter [" + formatterSettings
                            + "] for highlighter [" + highlighterName
                            + "]. Please verify the highlighter formatter setting at ["
                            + LuceneEnvironment.Highlighter.Formatter.TYPE + "]", e);
                }
            }
        }
        if (formatter instanceof CompassConfigurable) {
            ((CompassConfigurable) formatter).configure(settings);
        }
        return formatter;
    }
}
