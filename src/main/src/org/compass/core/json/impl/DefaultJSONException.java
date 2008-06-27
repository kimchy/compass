package org.compass.core.json.impl;

import org.compass.core.util.NestedRuntimeException;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 *
 * @author JSON.org
 * @version 2
 */
public class DefaultJSONException extends NestedRuntimeException {

    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public DefaultJSONException(String message) {
        super(message);
    }

    public DefaultJSONException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
