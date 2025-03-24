/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * Throw this exception when deserializing if the field cannot be successfully decrypted, e.g. corrupted
 */
public class JsonSecretException extends PersistenceException {
    /**
     * Constructor
     * @param message the message to show
     */
    public JsonSecretException(String message) {
        super(message);
    }
}
