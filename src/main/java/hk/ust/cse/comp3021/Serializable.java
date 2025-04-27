/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import hk.ust.cse.comp3021.exception.PersistenceException;
import org.json.JSONObject;

/**
 * Serializable interface
 */
public interface Serializable {
    /**
     * Convert the object to JSON
     * @return the JSON object
     */
    JSONObject toJSON();

    /**
     * Convert the object from JSON
     * @param jsonObject the JSON object
     * @throws PersistenceException if the JSON object is invalid when checking the annotations
     */
    void fromJSON(JSONObject jsonObject) throws PersistenceException;
}
