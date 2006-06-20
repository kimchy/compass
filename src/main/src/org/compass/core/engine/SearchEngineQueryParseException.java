package org.compass.core.engine;

import org.compass.core.CompassException;

/**
 * An exception happened when trying to parse a search engine query string.
 *
 * @author kimchy
 */
public class SearchEngineQueryParseException extends CompassException {

    private String queryString;

    public SearchEngineQueryParseException(String queryString, Throwable root) {
        super("Failed to parse query [" + queryString + "]", root);
        this.queryString = queryString;
    }

    public String getQueryString() {
        return this.queryString;
    }
}
