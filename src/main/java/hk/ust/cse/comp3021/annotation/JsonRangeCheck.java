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
 * This annotation is used to mark fields that should within the range during serialization and deserialization.
 * Any field annotated with {@link JsonRangeCheck} will be checked with the min and max.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonRangeCheck {
    /**
     * The minimum int value of the field
     * @return the minimum int value of the field
     */
    int minInt() default Integer.MIN_VALUE;

    /**
     * The maximum int value of the field
     * @return the maximum int value of the field
     */
    int maxInt() default Integer.MAX_VALUE;

    /**
     * The minimum long value of the field
     * @return the minimum long value of the field
     */
    long minLong() default Long.MIN_VALUE;

    /**
     * The maximum long value of the field
     * @return the maximum long value of the field
     */
    long maxLong() default Long.MAX_VALUE;

    /**
     * The minimum double value of the field
     * @return the minimum double value of the field
     */
    double minDouble() default Double.MIN_VALUE;

    /**
     * The maximum double value of the field
     * @return the maximum double value of the field
     */
    double maxDouble() default Double.MAX_VALUE;
}
