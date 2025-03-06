/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.annotation;

/**
 * This annotation is used to mark fields that should be filtered out during serialization.
 * Any field annotated with {@link JsonFilter} will be checked against keywords.
 */
public @interface JsonFilter {
    /**
     * The keyword list that you want to filter out
     *
     * @return the keyword list of the field
     */
    String[] kwList() default {"fuck", "shit", "damn", "bitch"};
}
