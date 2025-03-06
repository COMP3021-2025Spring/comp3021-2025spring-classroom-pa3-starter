/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.annotation;


/**
 * This annotation is used to mark fields that should within the range during serialization.
 * Any field annotated with {@link JsonRangeCheck} will checked with the min and max.
 */
public @interface JsonRangeCheck {
    /**
     * The minimum value of the field
     * @return the minimum value of the field
     */
    long min() default Long.MIN_VALUE;

    /**
     * The maximum value of the field, equals to 2025-06-01 00:00:00
     * @return the maximum value of the field
     */
    long max() default Long.MAX_VALUE;
}
