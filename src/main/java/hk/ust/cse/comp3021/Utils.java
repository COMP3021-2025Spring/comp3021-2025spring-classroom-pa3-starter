/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;


import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.*;

/**
 * Utils class
 */
public class Utils {
    /**
     * Print the content in green
     *
     * @param content the content to print
     * @return escaped content
     */
    public static String toInfo(String content) {
        return ansi().fg(GREEN).a(content).reset().toString();
    }

    /**
     * Print the content in green
     *
     * @param content the content to print
     */
    public static void printInfo(String content) {
        System.out.print(ansi().fg(GREEN).a(content).reset());
    }

    /**
     * Print the content in green and add a new line
     *
     * @param content the content to print
     */
    public static void printlnInfo(String content) {
        System.out.println(ansi().fg(GREEN).a(content).reset());
    }

    /**
     * Print the content in red and add a new line
     *
     * @param content the content to print
     */
    public static void printlnError(String content) {
        System.out.println(ansi().fg(RED).a(content).reset());
    }

    /**
     * Get the current time
     *
     * @return the current time
     */
    public static long getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * Convert the time to a string accepted by the file system
     * @param time the epoch time to convert
     * @return the string representation of the time
     */
    public static String timeToFilename(long time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC).format(formatter);
    }

    /**
     * Parse a JSONObject from file given by the clientUID
     * @param clientUID the clientUID
     * @return the JSONObject
     */
    public static JSONObject parseJSON(String clientUID) {
        try {
            Path filePath = Paths.get("sessions", clientUID + ".json");
            return new JSONObject(Files.readString(filePath));
        } catch (IOException e) {
            Utils.printlnError("Failed to load the session: " + e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * Write a JSONObject to file given by the clientUID
     * @param json the JSONObject
     * @param clientUID the clientUID
     */
    public static void writeJSON(JSONObject json, String clientUID) {
        Path filePath = Paths.get("sessions", clientUID + ".json");
        try {
            Files.writeString(filePath, json.toString(2));
            System.out.println("Session saved to " + filePath);
        } catch (IOException e) {
            Utils.printlnError("Failed to save the session: " + e.getMessage());
        }
    }
}
