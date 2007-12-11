package org.compass.annotations;

/**
 * The intenral Compass supported analyzers.
 * Note the {@link #CustomAnalyzer}, which is not an analyzer, but specifies
 * that the analyzer that will be used is a custom implementation of Lucene
 * <code>Analyzer</code>/
 *
 * @author kimchy
 */
public enum AnalyzerType {
    Standard,
    Simple,
    Whitespace,
    Stop,
    /**
     * Uses a snowball analyzer. See {@link SnowballType}.
     */
    Snowball,
    Braziliian,
    Cjk,
    Chinese,
    Czech,
    German,
    Greek,
    French,
    Dutch,
    Russian,
    CustomAnalyzer
}
