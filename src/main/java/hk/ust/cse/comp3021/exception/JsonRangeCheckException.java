/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * Throw this exception when deserializing if the field is out of range
 */
public class JsonRangeCheckException extends PersistenceException {
    /**
     * Constructor
     * @param message the message to show
     */
    public JsonRangeCheckException(String message) {
        super(message);
    }
}
