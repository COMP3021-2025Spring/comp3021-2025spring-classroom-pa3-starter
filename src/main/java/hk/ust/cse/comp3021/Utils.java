/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;


import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.*;

public class Utils {
    static void printGreen(String content) {
        System.out.print(ansi().fg(GREEN).a(content).reset());
    }

    static void printlnGreen(String content) {
        System.out.println(ansi().fg(GREEN).a(content).reset());
    }

    static void printRed(String content) {
        System.out.print(ansi().fg(RED).a(content).reset());
    }

    static void printlnRed(String content) {
        System.out.println(ansi().fg(RED).a(content).reset());
    }
}
