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

    /**
     * The profile time of admin when using thread pool stream processing
     */
    static long threadPoolProfileTime;

    @BeforeAll
    public static void setUp() {
        SessionManager.loadDatabase();
        // get base profile time
        LocalDateTime startProfile = LocalDateTime.now();
        SessionManager.generateProfileBase("admin");
        LocalDateTime endProfile = LocalDateTime.now();
        baseProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The base (serial) profiling cost %s ms %n", Utils.toInfo(String.valueOf(baseProfileTime)));
        // get parallel profile time
        startProfile = LocalDateTime.now();
        SessionManager.generateProfileParallel("admin");
        endProfile = LocalDateTime.now();
        parallelProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The parallel profiling cost %s ms %n", Utils.toInfo(String.valueOf(parallelProfileTime)));
        System.out.printf("The parallel profiling is %.2f times faster than the base profiling %n",
                (double) baseProfileTime / parallelProfileTime);
        // get thread pool profile time
        startProfile = LocalDateTime.now();
        SessionManager.generateProfileThreadPool("admin");
        endProfile = LocalDateTime.now();
        threadPoolProfileTime = Duration.between(startProfile, endProfile).toMillis();
        System.out.printf("The thread pool profiling cost %s ms %n",
                Utils.toInfo(String.valueOf(threadPoolProfileTime)));
        System.out.printf("The thread pool profiling is %.2f times faster than the base profiling %n",
                (double) baseProfileTime / threadPoolProfileTime);
        System.out.printf("The thread pool profiling is %.2f times faster than the parallel profiling %n",
                (double) parallelProfileTime / threadPoolProfileTime);
    }

    @Test
    public void testParallelProfileTime() {
        assertTrue(3 * parallelProfileTime <= baseProfileTime);
    }

    @Test
    public void testThreadPoolProfileTime() {
        assertTrue(threadPoolProfileTime <= parallelProfileTime * 1.25);
    }
}
