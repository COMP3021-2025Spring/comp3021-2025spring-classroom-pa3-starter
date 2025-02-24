/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;


import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.*;

/**
 * Utils class
 */
public class Utils {
    /**
     * Print the content in green
     * @param content the content to print
     */
    public static void printGreen(String content) {
        System.out.print(ansi().fg(GREEN).a(content).reset());
    }

    /**
     * Print the content in green and add a new line
     * @param content the content to print
     */
    public static void printlnGreen(String content) {
        System.out.println(ansi().fg(GREEN).a(content).reset());
    }

    /**
     * Print the content in red and add a new line
     * @param content the content to print
     */
    public static void printlnRed(String content) {
        System.out.println(ansi().fg(RED).a(content).reset());
    }

    /**
     * Get the current time
     * @return the current time
     */
    public static String getCurrentTime() {
        return String.valueOf(System.currentTimeMillis());
    }
}
