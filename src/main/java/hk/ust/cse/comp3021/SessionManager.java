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

/**
 * SessionManager class provides read/write interface for session database
 */
public class SessionManager {
    /**
     * The session database in memory as a JSONObject
     */
    static JSONObject sessions;

    final static String sessionsPath = "sessions.json";

    /**
     * Load sessions from file into memory, create a new file if not exist
     */
    static void loadSessions() {
        try {
            Path filePath = Paths.get(sessionsPath);
            sessions = new JSONObject(Files.readString(filePath));
            Utils.printlnInfo("Sessions database loaded");
        } catch (IOException | JSONException e) {
            Utils.printlnError("Failed to load the session: " + e.getMessage());
            sessions = new JSONObject();
            Utils.printlnInfo("Empty sessions database created");
        }
    }

    /**
     * Save sessions from memory to file
     */
    static void saveSessions() {
        try {
            Path filePath = Paths.get(sessionsPath);
            Files.writeString(filePath, sessions.toString(4));
            Utils.printlnInfo("Sessions database saved");
        } catch (IOException e) {
            Utils.printlnError("Fail to save sessions database");
        }
    }

    /**
     * Initialize an empty session database for a user
     *
     * @param user the user
     */
    static void initSessions(String user) {
        if (!sessions.has(user)) {
            sessions.put(user, new JSONObject());
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
            return sessions.getJSONObject(user).getJSONObject(sessionUID);
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
            sessions.getJSONObject(user).put(sessionUID, session);
        } catch (JSONException e) {
            Utils.printlnError("Failed to save the session: " + e.getMessage());
        }
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
            String timeLastOpened = Utils.timeToString(session.getLong("timeLastOpen"));
            String timeLastExit = Utils.timeToString(session.getLong("timeLastExit"));
            System.out.printf("UID: %s Client: %-20s Last Open: %s Last Exit: %s Tags: %-30s Description: %s %n",
                    Utils.toInfo(sessionUID), Utils.toInfo(clientName), Utils.toInfo(timeLastOpened),
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
        for (String sessionUID : sessions.getJSONObject(user).keySet()) {
            JSONObject session = getSession(user, sessionUID);
            if (session == null) {
                return;
            }
            printSession(sessionUID, session);
        }
    }

    /**
     * Generate a profile for the user and save as json file
     *
     * @param user the user to generate profile for
     */
    public static void generateProfile(String user) {
        Utils.printlnInfo("---- YOUR CHAT CLIENT PROFILE ----");
        listSessions(user);
    }
}
