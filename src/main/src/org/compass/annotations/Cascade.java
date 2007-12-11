package org.compass.annotations;

/**
 * Specifies cascading for an object relationship.
 *
 * @author kimchy
 */
public enum Cascade {
    /**
     * Perform cascading for all operations (create, save, delete).
     */
    ALL,

    /**
     * Perform cascading for create operations.
     */
    CREATE,

    /**
     * Perform cascading for save operations.
     */
    SAVE,

    /**
     * Perform cascading for delete operations.
     */
    DELETE
}
