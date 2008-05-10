package org.compass.core.engine;

/**
 * Acts as a marker interface for {@link java.io.Reader} where if calling close will
 * result in the reader going back to it's initial state.
 */
public interface RepeatableReader {
}
