package org.compass.needle.coherence;

/**
 * @author kimchy
 */
public interface FileKey {

    public static final byte FILE_HEADER = 0;

    public static final byte FILE_BUCKET = 1;

    public static final byte FILE_LOCK = 2;

    String getIndexName();

    String getFileName();

    byte getType();
}
