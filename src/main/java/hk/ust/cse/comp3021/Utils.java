/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.*;

/**
 * Utils class
 */
public class Utils {
    /**
     * Total seconds of Day
     */
    public static final int SoD = 86400;

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
     * Convert the time to a string
     *
     * @param time the epoch time to convert
     * @return the string representation of the time
     */
    public static String timeToString(long time) {
        if (time < SoD) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss a");
            return LocalTime.ofSecondOfDay(time).format(formatter);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC).format(formatter);
        }
    }

    /**
     * Get the duration between two times in minutes
     * @param startTime the start time
     * @param endTime the end time
     * @return the duration in minutes
     */
    public static int getDuration(long startTime, long endTime) {
        LocalDateTime start = LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.UTC);
        Duration duration = Duration.between(start, end);
        return (int) duration.toMinutes();
    }

    /**
     * Encrypt the text using XOR
     *
     * @param text the text to encrypt
     * @param key  the key to encrypt
     * @return the encrypted text
     */
    public static String encrypt(String text, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    /**
     * Decrypt the text using XOR, follows the same procedure of encryption
     *
     * @param text the text to decrypt
     * @param key  the key to decrypt
     * @return the decrypted text
     */
    public static String decrypt(String text, String key) {
        return encrypt(text, key);
    }

    /**
     * Check if the API key for genai platform is valid
     *
     * @param apiKey the API key
     * @return true if the API key is valid
     */
    public static boolean isValidApiKey(String apiKey) {
        return apiKey.matches("^[a-zA-Z0-9]{32}$");
    }

    /**
     * Generate a random UID for each session
     *
     * @return the UID, 32 characters long, with only numbers and letters
     */
    public static String generateUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
