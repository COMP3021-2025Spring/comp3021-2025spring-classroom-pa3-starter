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
 * This annotation is used to mark fields that should be encrypted during serialization and decrypted during deserialization.
 * Any field annotated with {@link JsonSecret} will be encrypted using XOR algorithm.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonSecret {
    /**
     * The secret key used to encrypt the field
     *
     * @return the secret key of the field
     */
    String key() default "comp3021";
}
