/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * Throw this exception when deserializing if the field has been changed after serialization
 */
public class JsonCheckException extends PersistenceException {
    /**
     * Constructor
     * @param message the message to show
     */
    public JsonCheckException(String message) {
        super(message);
    }
}
