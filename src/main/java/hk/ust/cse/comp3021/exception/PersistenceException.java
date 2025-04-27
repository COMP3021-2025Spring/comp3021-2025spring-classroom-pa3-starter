/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.exception;

/**
 * The common interface for all exceptions related to persistence
 */
public class PersistenceException extends Exception {
    /**
     * Constructor
     * @param message the message to show
     */
    public PersistenceException(String message) {
        super(message);
    }
}



