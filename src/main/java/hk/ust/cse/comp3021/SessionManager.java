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
import java.util.function.*;
import java.util.stream.Stream;

/**
 * SessionManager class provides read/write interface for session database
 */
public class SessionManager {
    /**
     * The session database in memory as a JSONObject
     */
    static JSONObject db;

    /**
     * The path to the session database file
     */
    final static String dbPath = "db.json";

    /**
     * Load session database from file into memory, create an empty database if not exist
     */
    static void loadDatabase() {
        try {
            Path filePath = Paths.get(dbPath);
            db = new JSONObject(Files.readString(filePath));
            Utils.printlnInfo("Sessions database loaded");
        } catch (IOException | JSONException e) {
            Utils.printlnError("Failed to load the session: " + e.getMessage());
            db = new JSONObject();
            Utils.printlnInfo("Empty sessions database created");
        }
    }

    /**
     * Save session database from memory to file
     */
    static void saveDatabase() {
        try {
            Path filePath = Paths.get(dbPath);
            Files.writeString(filePath, db.toString(4));
            Utils.printlnInfo("Sessions database saved");
        } catch (IOException e) {
            Utils.printlnError("Fail to save sessions database");
        }
    }

    /**
     * Initialize sessions for current user
     *
     * @param user the user
     */
    static void initSessions(String user) {
        if (!db.has(user)) {
            db.put(user, new JSONObject());
        }
    }

    /**
     * Get the session given the user and sessionUID
     *
     * @param user       the user to get session for
     * @param sessionUID the sessionUID to get
     * @return the session
     */
    static JSONObject getSession(String user, String sessionUID) {
        try {
            return db.getJSONObject(user).getJSONObject(sessionUID);
        } catch (JSONException e) {
            Utils.printlnError("Failed to get the session: " + e.getMessage());
        }
        return null;
    }

    /**
     * Save a session to the session database
     *
     * @param user    the user to save session for
     * @param session the session to save
     */
    static void setSession(String user, String sessionUID, JSONObject session) {
        try {
            db.getJSONObject(user).put(sessionUID, session);
        } catch (JSONException e) {
            Utils.printlnError("Failed to save the session: " + e.getMessage());
        }
    }

    /**
     * Get all the sessions of the user as stream
     *
     * @param user the user to get sessions for
     * @return the sessions
     */
    static Stream<JSONObject> getSessionsStream(String user) {
        return db.getJSONObject(user).toMap().keySet().stream().map(uid -> getSession(user, uid));
    }

    /**
     * Get all the sessions of all users as stream
     *
     * @return the sessions
     */
    static Stream<JSONObject> getSessionsStream() {
        return db.toMap().entrySet().stream().flatMap(e -> getSessionsStream(e.getKey()));
    }

    /**
     * Print the session information
     *
     * @param session the session to print
     */
    static void printSession(String sessionUID, JSONObject session) {
        try {
            String clientName = session.getString("clientName");
            String tags = session.getJSONArray("tags").join(", ");
            String description = session.getString("description");
            String timeCreated = Utils.timeToString(session.getLong("timeCreated"));
            String timeLastExit = Utils.timeToString(session.getLong("timeLastExit"));
            System.out.printf("UID: %s Client: %-20s Created: %s Last Exit: %s Tags: %-30s Description: %s %n",
                    Utils.toInfo(sessionUID), Utils.toInfo(clientName), Utils.toInfo(timeCreated),
                    Utils.toInfo(timeLastExit),
                    Utils.toInfo(tags), Utils.toInfo(description));
        } catch (JSONException e) {
            Utils.printlnError("Error reading session: " + e.getMessage());
        }
    }

    /**
     * List all the previously stored sessions
     */
    public static void listSessions(String user) {
        db.getJSONObject(user).toMap().forEach((key, value) -> printSession(key, getSession(user, key)));
    }

    /**
     * Get the number of users
     */
    public static int getNumUsers() {
        return db.toMap().size();
    }

    /**
     * Get the number of sessions of the user
     *
     * @param user the user to get number of sessions for
     * @return the number of sessions
     */
    public static long getNumSessions(String user) {
        return getSessionsStream(user).count();
    }

    /**
     * Get the number of sessions of all users
     *
     * @return the number of sessions
     */
    public static long getNumSessions() {
        return getSessionsStream().count();
    }

    /**
     * Get the statistics of the user using map-reduce
     *
     * @param user the user to get statistics for
     * @param m    the map function
     * @param i    the initial reduce value
     * @param r    the reduce function
     * @return the statistics
     */
    public static int getStat(String user, ToIntFunction<JSONObject> m, int i, IntBinaryOperator r) {
        return getSessionsStream(user).mapToInt(m).reduce(i, r);
    }

    /**
     * Get the statistics of all users using map-reduce
     *
     * @param m the map function
     * @param i the initial reduce value
     * @param r the reduce function
     * @return the statistics
     */
    public static int getStat(ToIntFunction<JSONObject> m, int i, IntBinaryOperator r) {
        return getSessionsStream().mapToInt(m).reduce(i, r);
    }

    /**
     * Generate a profile for the user and save as json file
     *
     * @param user the user to generate profile for
     */
    public static void generateProfile(String user) {
        JSONObject profile = new JSONObject();

        profile.put("numSessions", getNumSessions(user));
        profile.put("sumPromptTokens", getStat(user, s -> s.getInt("totalPromptTokens"), 0, Integer::sum));
        profile.put("sumCompletionTokens", getStat(user, s -> s.getInt("totalCompletionTokens"), 0, Integer::sum));
        profile.put("maxPromptTokens", getStat(user, s -> s.getInt("totalPromptTokens"), Integer.MIN_VALUE, Math::max));
        profile.put("maxCompletionTokens", getStat(user, s -> s.getInt("totalCompletionTokens"), Integer.MIN_VALUE, Math::max));
        profile.put("minPromptTokens", getStat(user, s -> s.getInt("totalPromptTokens"), Integer.MAX_VALUE, Math::min));
        profile.put("minCompletionTokens", getStat(user, s -> s.getInt("totalCompletionTokens"), Integer.MAX_VALUE, Math::min));
        profile.put("averagePromptTokens", (double) getStat(user, s -> s.getInt("totalPromptTokens"), 0, Integer::sum) / getNumSessions(user));
        profile.put("averageCompletionTokens", (double) getStat(user, s -> s.getInt("totalCompletionTokens"), 0, Integer::sum) / getNumSessions(user));

        System.out.println("----- YOUR CHAT CLIENT PROFILE -----");
        System.out.println(profile.toString(2));

        try {
            Path filePath = Paths.get(user + "-profile.json");
            Files.writeString(filePath, profile.toString(2));
            System.out.println("Profile generated at " + Utils.toInfo(filePath.toString()));
        } catch (IOException e) {
            Utils.printlnError("Fail to generate profile for " + user);
        }
    }

    /**
     * Show the status of the session database
     */
    public static void generateProfile() {
        JSONObject profile = new JSONObject();

        profile.put("numSessions", getNumSessions());
        profile.put("numUsers", getNumUsers());
        profile.put("averageSessionsPerUser", getNumSessions() / getNumUsers());
        profile.put("sumPromptTokens", getStat(s -> s.getInt("totalPromptTokens"), 0, Integer::sum));
        profile.put("sumCompletionTokens", getStat(s -> s.getInt("totalCompletionTokens"), 0, Integer::sum));
        profile.put("maxPromptTokens", getStat(s -> s.getInt("totalPromptTokens"), Integer.MIN_VALUE, Math::max));
        profile.put("maxCompletionTokens", getStat(s -> s.getInt("totalCompletionTokens"), Integer.MIN_VALUE, Math::max));
        profile.put("minPromptTokens", getStat(s -> s.getInt("totalPromptTokens"), Integer.MAX_VALUE, Math::min));
        profile.put("minCompletionTokens", getStat(s -> s.getInt("totalCompletionTokens"), Integer.MAX_VALUE, Math::min));
        profile.put("averagePromptTokens", (double) getStat(s -> s.getInt("totalPromptTokens"), 0, Integer::sum) / getNumSessions());
        profile.put("averageCompletionTokens", (double) getStat(s -> s.getInt("totalCompletionTokens"), 0, Integer::sum) / getNumSessions());

        System.out.println("----- SESSION DATABASE PROFILE -----");
        System.out.println(profile.toString(2));

        try {
            Path filePath = Paths.get("system-profile.json");
            Files.writeString(filePath, profile.toString(2));
            System.out.println("Profile generated at " + Utils.toInfo(filePath.toString()));
        } catch (IOException e) {
            Utils.printlnError("Fail to generate profile for the session database");
        }
    }
}
