/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.annotation;

/**
 * This annotation is used to mark fields that should be encrypted during serialization.
 * Any field annotated with {@link JsonSecret} will be encrypted.
 */
public @interface JsonSecret {
    /**
     * The secret key used to encrypt the field
     *
     * @return the secret key of the field
     */
    String key() default "";
}
