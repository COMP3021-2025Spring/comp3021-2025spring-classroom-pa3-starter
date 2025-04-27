/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * Throw this exception when we are asked to create a client with an invalid name
 */
public class InvalidClientNameException extends Exception {
    /**
     * Constructor
     * @param message the message to show
     */
    public InvalidClientNameException(String message) {
        super(message);
    }
}

