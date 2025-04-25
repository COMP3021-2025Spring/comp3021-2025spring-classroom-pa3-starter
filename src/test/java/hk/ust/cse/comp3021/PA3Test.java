/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PA3Test {
    /**
     * The profile time of admin when using serial stream processing, modify it based on your running environment
     */
    final static long serialProfileTime = 13408;

    @BeforeAll
    public static void setUp() {
        SessionManager.loadDatabase();
    }

    @Test
    public void testAdminProfileTime() {
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfile("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        long parallelProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The profiling cost %s ms %n", Utils.toInfo(String.valueOf(parallelProfileTime)));
        assertTrue(3 * parallelProfileTime <= serialProfileTime);
    }
}
