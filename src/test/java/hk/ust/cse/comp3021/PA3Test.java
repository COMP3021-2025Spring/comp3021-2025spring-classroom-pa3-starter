/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PA3Test {
    /**
     * The profile time of admin when using serial stream processing
     */
    static long baseProfileTime;

    /**
     * The profile time of admin when using parallel stream processing
     */
    static long parallelProfileTime;

    @BeforeAll
    public static void setUp() {
        SessionManager.loadDatabase();
    }

    @Test
    @Order(1)
    public void testBaseProfileTime() {
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfileBase("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        baseProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The base (serial) profiling cost %s ms %n", Utils.toInfo(String.valueOf(baseProfileTime)));
    }

    @Test
    @Order(2)
    public void testParallelProfileTime() {
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfileParallel("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        parallelProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The parallel profiling cost %s ms %n", Utils.toInfo(String.valueOf(parallelProfileTime)));
        assertTrue(3 * parallelProfileTime <= baseProfileTime);
    }

    @Test
    @Order(3)
    public void testThreadPoolProfileTime() {
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfileThreadPool("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        long threadPoolProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The thread pool profiling cost %s ms %n", Utils.toInfo(String.valueOf(threadPoolProfileTime)));
        assertTrue(threadPoolProfileTime <= parallelProfileTime);
    }
}
