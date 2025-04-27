/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * Throw this exception when deserializing if the field contains prohibited content
 */
public class JsonFilterException extends PersistenceException {
    /**
     * Constructor
     * @param message the message to show
     */
    public JsonFilterException(String message) {
        super(message);
    }
}
