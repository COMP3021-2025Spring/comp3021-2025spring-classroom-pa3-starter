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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        System.out.printf("The parallel profiling is %s times faster than the base profiling %n",
                Utils.toInfo(String.valueOf((double) baseProfileTime / parallelProfileTime)));
        assertTrue(3 * parallelProfileTime <= baseProfileTime);
    }

    @Test
    @Order(3)
    public void testThreadPoolProfileTime() {
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfileThreadPool("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        long threadPoolProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The thread pool profiling cost %s ms %n",
                Utils.toInfo(String.valueOf(threadPoolProfileTime)));
        System.out.printf("The thread pool profiling is %s times faster than the base profiling %n",
                Utils.toInfo(String.valueOf((double) baseProfileTime / threadPoolProfileTime)));
        System.out.printf("The thread pool profiling is %s times faster than the parallel profiling %n",
                Utils.toInfo(String.valueOf((double) parallelProfileTime / threadPoolProfileTime)));
        assertTrue(threadPoolProfileTime <= parallelProfileTime * 1.25);
    }
}
