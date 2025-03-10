/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark fields that should be filtered out during serialization.
 * Any field annotated with {@link JsonFilter} will be checked against pre-defined prohibited keywords.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonFilter {
    /**
     * The keyword list that you want to filter out
     *
     * @return the keyword list of the field
     */
    String[] kwList() default {"fuck", "shit", "damn", "bitch"};
}
