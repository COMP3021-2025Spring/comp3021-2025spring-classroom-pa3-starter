/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;


import org.json.JSONException;
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
     *
     * @param time the epoch time to convert
     * @return the string representation of the time
     */
    public static String timeToFilename(long time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC).format(formatter);
    }

    /**
     * Convert the time to a string
     *
     * @param time the epoch time to convert
     * @return the string representation of the time
     */
    public static String timeToString(long time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC).format(formatter);
    }

    /**
     * Parse a JSONObject from file given by the clientUID
     *
     * @param clientUID the clientUID
     * @return the JSONObject
     */
    public static JSONObject parseJSON(String clientUID) {
        try {
            Path filePath = Paths.get("sessions", clientUID + ".json");
            return new JSONObject(Files.readString(filePath));
        } catch (JSONException e) {
            Utils.printlnError("Failed to parse the session: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Utils.printlnError("Failed to load the session: " + e.getMessage());
            return null;
        }
    }

    /**
     * Write a JSONObject to file given by the clientUID
     *
     * @param json      the JSONObject
     * @param clientUID the clientUID
     */
    public static void writeJSON(JSONObject json, String clientUID) {
        Path filePath = Paths.get("sessions", clientUID + ".json");
        try {
            Files.writeString(filePath, json.toString(2));
        } catch (IOException e) {
            Utils.printlnError("Failed to save the session: " + e.getMessage());
        }
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
     * @param apiKey the API key
     * @return true if the API key is valid
     */
    public static boolean isValidApiKey(String apiKey) {
        return apiKey.matches("^[a-zA-Z0-9]{32}$");
    }
}
