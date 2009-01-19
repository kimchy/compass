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

/**
 * A highlighter tool that can highlight hits in a given text based on an
 * executed query.
 * 
 * @see org.compass.core.CompassHits#highlighter(int)
 * 
 * @author kimchy
 */
public interface CompassHighlighter {

    /**
     * Controls the way text will be tokenized in order to perform the highlight
     * operation.
     * 
     * @author kimchy
     */
    public static enum TextTokenizer {


        /** Forces tokenization of the text using the analyzer. */
        ANALYZER,

        /** Forces tokenization of the text using the tem vector information. */
        TERM_VECTOR,

        /**
         * Will use term vector if available to tokenize the text, otherwise
         * will use analyzer.
         */
        AUTO;

        public static String toString(TextTokenizer textTokenizer) {
            if (textTokenizer == CompassHighlighter.TextTokenizer.ANALYZER) {
                return "analyzer";
            } else if (textTokenizer == CompassHighlighter.TextTokenizer.TERM_VECTOR) {
                return "term_vector";
            } else if (textTokenizer == CompassHighlighter.TextTokenizer.AUTO) {
                return "auto";
            }
            throw new IllegalArgumentException("Can't find text tokenizer for [" + textTokenizer + "]");
        }

        public static CompassHighlighter.TextTokenizer fromString(String textTokenizer) {
            if ("analyzer".equalsIgnoreCase(textTokenizer)) {
                return CompassHighlighter.TextTokenizer.ANALYZER;
            } else if ("term_vector".equalsIgnoreCase(textTokenizer)) {
                return CompassHighlighter.TextTokenizer.TERM_VECTOR;
            } else if ("auto".equalsIgnoreCase(textTokenizer)) {
                return CompassHighlighter.TextTokenizer.AUTO;
            }
            throw new IllegalArgumentException("Can't find text tokenizer for [" + textTokenizer + "]");
        }
    }

    /**
     * Sets the highlighter that will be used out the ones set in the
     * configuration. The highlighters are groups of pre-set configurations for
     * the sepcified highlighter. The default one is called <code>default</code>.
     * 
     * @param highlighterName
     *            The name of the highlighter that will be used
     * @return the higlighter
     * @throws CompassException
     */
    CompassHighlighter setHighlighter(String highlighterName) throws CompassException;

    /**
     * Sets the analyzer that will be used if analysis of the text is needed
     * (see {@link TextTokenizer}).
     * 
     * @param analyzerName
     *            The analyzer name that will be used.
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setAnalyzer(String analyzerName) throws CompassException;

    /**
     * Sets the analyzer that will be used if analysis of the text is needed
     * (see {@link TextTokenizer}). Uses the resource to derive the analyzer
     * that will be used (works also with per resource property analyzer).
     *
     * @param resource The resource to derive the analyzer from
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setAnalyzer(Resource resource) throws CompassException;

    /**
     * Sets the separator string that will be used to combine different
     * fragments in {@link #fragmentsWithSeparator(String)}. If not set, will
     * use the separator configured for the chosen highlighter.
     * 
     * @param separator
     *            The separator used
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setSeparator(String separator) throws CompassException;

    /**
     * Sets the maximum number of bytes that will be analyzed for highlighting.
     * If not set, will use the value configured for the chosen highlighter.
     * 
     * @param maxBytesToAnalyze
     *            The maximum number of bytes analyzed for highlighting
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setMaxBytesToAnalyze(int maxBytesToAnalyze) throws CompassException;

    /**
     * Sets the maximum number of fragments that can be returned or combined to
     * a separator. If not set, will use the value configured for the chosen
     * highlighter.
     * 
     * @param maxNumFragments
     *            The maximum number if fragments
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setMaxNumFragments(int maxNumFragments) throws CompassException;

    /**
     * Sets how the text will be tokenized for highlighting. If not set, will
     * use the value configured for the chosen highlighter.
     * 
     * @param textTokenizer
     *            How the text will be tokenized for highlighting
     * @return the highlighter
     * @throws CompassException
     */
    CompassHighlighter setTextTokenizer(CompassHighlighter.TextTokenizer textTokenizer) throws CompassException;

    /**
     * Returns the best highlighted fragment for the given property name /
     * meta-data. The highlighted text will be retrived from the index, so it
     * must be stored.
     * <p>
     * Note, if there are more than one resource property name / meta-data with
     * the same name, the text will be taken from the first one.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param propertyName
     *            The resource property name / meta-data.
     * @return The best fragment text highlighted.
     * @throws CompassException
     */
    String fragment(String propertyName) throws CompassException;

    /**
     * Returns the best highlighted fragment for the given property name /
     * meta-data. The given text will be used for highlight. Handy when the text
     * is not stored in the index.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param propertyName
     *            The resource property name / meta-data.
     * @param text
     *            The text to be highlighted.
     * @return The best fragment text highlighted.
     * @throws CompassException
     */
    String fragment(String propertyName, String text) throws CompassException;

    /**
     * Returns the best highlighted fragments for the given property name /
     * meta-data. The highlighted text will be retrived from the index, so it
     * must be stored.
     * <p>
     * Note, that the number of fragments will be between <code>0</code> and
     * <code>maxNumFragments</code>.
     * <p>
     * Note, if there are more than one resource property name / meta-data with
     * the same name, the text will be taken from the first one.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param propertyName
     *            The resource property name / meta-data.
     * @return The best fragments highlighted.
     * @throws CompassException
     */
    String[] fragments(String propertyName) throws CompassException;

    /**
     * Returns the best highlighted fragments for the given property name /
     * meta-data. The given text will be used for highlight.
     * <p>
     * Note, that the number of fragments will be between <code>0</code> and
     * <code>maxNumFragments</code>.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param propertyName
     *            The resource property name / meta-data.
     * @param text
     *            The text to be highlighted.
     * @return The best fragments highlighted.
     * @throws CompassException
     */
    String[] fragments(String propertyName, String text) throws CompassException;

    /**
     * Returns the best highlighted fragments for the given property name /
     * meta-data, separated with the given separator. The highlighted text will
     * be retrived from the index, so it must be stored.
     * <p>
     * Note, that the number of fragments will be between <code>0</code> and
     * <code>maxNumFragments</code>.
     * <p>
     * Note, if there are more than one resource property name / meta-data with
     * the same name, the text will be taken from the first one.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     *
     * @param propertyName
     *            The resource property name / meta-data.
     * @return The best fragments highlighted and separated.
     * @throws CompassException
     */
    String fragmentsWithSeparator(String propertyName) throws CompassException;

    /**
     * Returns the best highlighted fragments for the given property name /
     * meta-data, separated with the given separator. The given text will be
     * used for highlight.
     * <p>
     * Note, that the number of fragments will be between <code>0</code> and
     * <code>maxNumFragments</code>.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     * 
     * @param propertyName
     *            The resource property name / meta-data.
     * @param text
     *            The text to be highlighted.
     * @return The best fragments highlighted and separated.
     * @throws CompassException
     */
    String fragmentsWithSeparator(String propertyName, String text) throws CompassException;
    
    /**
	 * Returns the best highlighted fragment of each matching <i>multi</i> resource
	 * property name / meta-data (i.e.: when there is more then one property of the
	 * same name). The highlighted texts will be retrived from the index, so it must
	 * be stored.
     * <p>
     * Note, that the number of returned fragments is not limited by
     * <code>maxNumFragments</code> value.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     * 
     * @param propertyName
     * 				The resource property name / meta-data.
     * @return The best fragment texts highlighted.
     * @throws CompassException
     */
    String[] multiValueFragment(String propertyName) throws CompassException;
    
    /**
     * Returns the best highlighted fragment of each matching <i>multi</ib> resource
	 * property name / meta-data (i.e.: when there is more then one property of the
	 * same name). The given texts will be used for highlight. Handy when the texts
     * are not stored in the index.
     * <p>
     * Note, that the number of returned fragments is not limited by
     * <code>maxNumFragments</code> value.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     * 
     * @param propertyName
     * 				The resource property name / meta-data.
     * @param texts
     * 				Texts to be highlighted.
     * @return The best fragment texts highlighted.
     * @throws CompassException
     */
    String[] multiValueFragment(String propertyName, String[] texts) throws CompassException;
    
    /**
     * Returns the best highlighted fragments for the given <i>multi</i> property
     * name / meta-data, separated with the given separator. The highlighted text
     * will be retrived from the index, so it must be stored.
     * <p>
     * Note, that the number of separeted fragments is not
     * limited by <code>maxNumFragments</code> value.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     * 
     * @param propertyName
     * 				The resource property name / meta-data.
     * @return The best fragments highlighted and separated.
     * @throws CompassException
     */
    String multiValueFragmentWithSeparator(String propertyName) throws CompassException;
    
    /**
     * Returns the best highlighted fragments for the given <i>multi</i> property
     * name / meta-data, separated with the given separator. The given texts will
     * be used for highlight.
     * <p>
     * Note, that the number of fragments contained in returned string is not
     * limited by <code>maxNumFragments</code> value.
     * <p>
     * The name can either be the actual resource property or meta-data value,
     * or the path to the given resource property (alias.rProperty), or the
     * class property (alias.cProperty) or the path to the meta-data
     * (alias.cProperty.metaData)
     * 
     * @param propertyName
     * 				The resource property name / meta-data.
     * @param texts
     * 				Texts to be highlighted.
     * @return The best fragments highlighted and separated.
     * @throws CompassException
     */
    String multiValueFragmentWithSeparator(String propertyName, String[] texts) throws CompassException;

}
