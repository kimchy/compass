package org.compass.needle.gae;

import java.util.ConcurrentModificationException;

/**
 * This exception arises when the number of prescribed attempts to complete a
 * transaction have been used.
 *
 * @author patricktwohig
 */
public class GoogleAppEngineAttemptsExpiredException extends GoogleAppEngineDirectoryException {

    public GoogleAppEngineAttemptsExpiredException(String message, ConcurrentModificationException e) {
        super(message, e);
    }

    public GoogleAppEngineAttemptsExpiredException(String indexName, String fileName, String message,
                                                   ConcurrentModificationException e) {
        super(indexName, fileName, message, e);
    }

    public GoogleAppEngineAttemptsExpiredException(String indexName, String fileName, String message) {
        super(indexName, fileName, message);
    }

    public GoogleAppEngineAttemptsExpiredException(String message) {
        super(message);
    }

}
