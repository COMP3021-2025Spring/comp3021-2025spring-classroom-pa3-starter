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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    static final String dbPath = "db.json";

    /**
     * Load session database from file into memory, create an empty database if not exist
     */
    static void loadDatabase() {
        try {
            Path filePath = Paths.get(dbPath);
            db = new JSONObject(Files.readString(filePath));
            Utils.printlnInfo("Sessions database loaded");
        } catch (IOException | JSONException e) {
            Utils.printlnError("Failed to load the database: " + e.getMessage());
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
        if (Objects.equals(user, "admin"))
            return db.toMap()
                    .keySet()
                    .stream()
                    .flatMap(SessionManager::getSessionsStream);
        else
            return db.getJSONObject(user).toMap()
                    .keySet()
                    .stream()
                    .map(uid -> getSession(user, uid));
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
                    Utils.toInfo(timeLastExit), Utils.toInfo(tags), Utils.toInfo(description));
        } catch (JSONException e) {
            Utils.printlnError("Error reading session: " + e.getMessage());
        }
    }

    /**
     * List all the previously stored sessions
     *
     * @param user the user to list sessions for
     */
    public static void listSessions(String user) {
        db.getJSONObject(user).toMap()
                .forEach((key, value) ->
                        printSession(key, Objects.requireNonNull(getSession(user, key))));
    }

    /**
     * Get the number of users
     *
     * @return the number of users
     */
    public static int getNumUsers() {
        return db.toMap().size();
    }

    /**
     * Get the set of users
     *
     * @return the set of users
     */
    public static Set<String> getUsers() {
        return db.toMap().keySet();
    }

    /**
     * The list of ignored words when counting top words
     */
    static List<String> ignoredWords;

    // Load the stopwords from the file stopwords.txt, remember to filter out comments
    static {
        try {
            ignoredWords = Files.readAllLines(Paths.get("stopwords.txt"));
        } catch (IOException e) {
            Utils.printlnError("Failed to load stopwords: " + e.getMessage());
            ignoredWords = new ArrayList<>();
        }
    }

    /**
     * The unit USD price of a prompt token using one billion parameters
     */
    static final Double unitPromptPrice = 8.82e-09;

    /**
     * The unit USD price of a response token using one billion parameters
     */
    static final Double unitCompletionPrice = 3.53e-08;

    /**
     * Initialize an empty profile
     *
     * @return the empty profile
     */
    static JSONObject createEmptyProfile() {
        return new JSONObject()
                .put("numSessions", 0)
                .put("sumPromptTokens", 0)
                .put("sumCompletionTokens", 0)
                .put("sumTemperature", 0.0)
                .put("sumTimeCreated", 0)
                .put("sumTimeLastExit", 0)
                .put("sumTimeLastOpen", 0)
                .put("sumLastSessionDuration", 0)
//                .put("sumPrice", 0.0)
                .put("maxPromptTokens", Integer.MIN_VALUE)
                .put("maxCompletionTokens", Integer.MIN_VALUE)
                .put("maxTimeCreated", Integer.MIN_VALUE)
                .put("maxTimeLastOpen", Integer.MIN_VALUE)
                .put("maxTimeLastExit", Integer.MIN_VALUE)
                .put("minPromptTokens", Integer.MAX_VALUE)
                .put("minCompletionTokens", Integer.MAX_VALUE)
                .put("minTimeCreated", Integer.MAX_VALUE)
                .put("minTimeLastOpen", Integer.MAX_VALUE)
                .put("minTimeLastExit", Integer.MAX_VALUE)
                .put("topTags", new JSONObject())
                .put("topWords", new JSONObject())
                .put("topModels", new JSONObject());
    }

    /**
     * Update the top string map with new strings
     *
     * @param topStringMap the top string map, from its
     * @param newStrings   the new strings to add
     * @return the updated top string map
     */
    private static JSONObject updateTopString(JSONObject topStringMap, Stream<String> newStrings) {
        newStrings.forEach(topStringMap::increment);
        return topStringMap;
    }

    /**
     * Keep only the top N strings in the map
     *
     * @param topNStringMap the top N string map
     * @param topN          the number of top strings to keep
     * @return the updated top N string map
     */
    private static JSONObject limitTopNString(JSONObject topNStringMap, int topN) {
        return topNStringMap.toMap().entrySet().stream()
                .sorted((e1, e2) -> Integer.compare((Integer) e2.getValue(), (Integer) e1.getValue()))
                .limit(topN)
                .reduce(new JSONObject(),
                        (acc, entry) -> acc.put(entry.getKey(), entry.getValue()),
                        (acc1, acc2) -> acc1);
    }

    /**
     * Generate a profile for the user and save as json file using reduce
     * Casting the epoch time from Long to Integer is safe because we will not encounter the
     * <a href="https://en.wikipedia.org/wiki/Year_2038_problem">Year 2038 problem</a>.
     *
     * @param user the user to generate profile for
     */
    static JSONObject generateProfile(String user) {
        // iterate through all sessions and collect statistics
        JSONObject profile = getSessionsStream(user)
                .reduce(createEmptyProfile(), (p, s) -> p
                        // get sum statistics
                        .increment("numSessions")
                        .put("sumPromptTokens", p.getInt("sumPromptTokens") + s.getInt("totalPromptTokens"))
                        .put("sumCompletionTokens", p.getInt("sumCompletionTokens") + s.getInt("totalCompletionTokens"))
                        .put("sumTemperature", p.getDouble("sumTemperature") + s.getDouble("temperature"))
                        .put("sumTimeCreated", p.getInt("sumTimeCreated") + s.getInt("timeCreated") % Utils.SoD)
                        .put("sumTimeLastOpen", p.getInt("sumTimeLastOpen") + s.getInt("timeLastOpen") % Utils.SoD)
                        .put("sumTimeLastExit", p.getInt("sumTimeLastExit") + s.getInt("timeLastExit") % Utils.SoD)
                        .put("sumLastSessionDuration", p.getInt("sumLastSessionDuration") +
                                Utils.getDuration(s.getInt("timeLastOpen"), s.getInt("timeLastExit")))
//                        .put("sumPrice", p.getDouble("sumPrice")
//                                + (s.getInt("totalPromptTokens") * unitPromptPrice
//                                + s.getInt("totalCompletionTokens") * unitCompletionPrice)
//                                * Integer.parseInt(s.getString("clientName").split("[-b]")[1]))
                        // get max statistics
                        .put("maxPromptTokens", Math.max(p.getInt("maxPromptTokens"),
                                s.getInt("totalPromptTokens")))
                        .put("maxCompletionTokens", Math.max(p.getInt("maxCompletionTokens"),
                                s.getInt("totalCompletionTokens")))
                        .put("maxTimeCreated", Math.max(p.getInt("maxTimeCreated"),
                                s.getInt("timeCreated")))
                        .put("maxTimeLastOpen", Math.max(p.getInt("maxTimeLastOpen"),
                                s.getInt("timeLastOpen")))
                        .put("maxTimeLastExit", Math.max(p.getInt("maxTimeLastExit"),
                                s.getInt("timeLastExit")))
                        // get min statistics
                        .put("minPromptTokens", Math.min(p.getInt("minPromptTokens"),
                                s.getInt("totalPromptTokens")))
                        .put("minCompletionTokens", Math.min(p.getInt("minCompletionTokens"),
                                s.getInt("totalCompletionTokens")))
                        .put("minTimeCreated", Math.min(p.getInt("minTimeCreated"),
                                s.getInt("timeCreated")))
                        .put("minTimeLastOpen", Math.min(p.getInt("minTimeLastOpen"),
                                s.getInt("timeLastOpen")))
                        .put("minTimeLastExit", Math.min(p.getInt("minTimeLastExit"),
                                s.getInt("timeLastExit")))
                        // get top String statistics
                        .put("topTags", updateTopString(p.getJSONObject("topTags"),
                                s.getJSONArray("tags").toList().stream().map(String::valueOf)))
                        .put("topWords", updateTopString(p.getJSONObject("topWords"),
                                Arrays.stream(s.getJSONObject("messages")
                                                .getJSONArray("contents")
                                                .join(" ")
                                                .split("\\s+"))
                                        .filter(str -> !ignoredWords.contains(str.toLowerCase()))
                                        .filter(str -> str.matches("[a-zA-Z]+"))))
                        .put("topModels", updateTopString(p.getJSONObject("topModels"),
                                Stream.of(s.getString("clientName").split("-")[0])))
                );
        // post process
        int numSessions = profile.getInt("numSessions");
        // get average statistics
        if (numSessions != 0) {
            profile
                .put("avgTemperature", profile.getDouble("sumTemperature") / numSessions)
                .put("avgTimeLastOpen", profile.getInt("sumTimeLastOpen") / numSessions)
                .put("avgTimeCreated", profile.getInt("sumTimeCreated") / numSessions)
                .put("avgTimeLastExit", profile.getInt("sumTimeLastExit") / numSessions)
                .put("avgLastSessionDuration", profile.getInt("sumLastSessionDuration") / numSessions)
                .put("avgPromptTokens", profile.getInt("sumPromptTokens") / numSessions)
                .put("avgCompletionTokens", profile.getInt("sumCompletionTokens") / numSessions);
        } else {
            profile.put("avgTemperature", 0.0)
                    .put("avgTimeLastOpen", 0)
                    .put("avgTimeCreated", 0)
                    .put("avgTimeLastExit", 0)
                    .put("avgLastSessionDuration", 0)
                    .put("avgPromptTokens", 0)
                    .put("avgCompletionTokens", 0);
        }

        // admin only statistics
        if (user.equals("admin")) {
            profile.put("numUsers", getNumUsers());
            profile.put("avgSessions", profile.getInt("numSessions") / getNumUsers());
        }

        // remove useless statistics
        return profile
                .put("sumTemperature", (Object) null)
                .put("sumTimeCreated", (Object) null)
                .put("sumTimeLastOpen", (Object) null)
                .put("sumTimeLastExit", (Object) null)
                .put("sumLastSessionDuration", (Object) null)
                // filter the topN strings
                .put("topTags", limitTopNString(profile.getJSONObject("topTags"), 3))
                .put("topWords", limitTopNString(profile.getJSONObject("topWords"), 20))
                .put("topModels", limitTopNString(profile.getJSONObject("topModels"), 5));
    }

    /**
     * Print the profile in human-readable form
     * TODO: print the time as human-readable form
     *
     * @param profile the JSON format profile to print
     */
    private static void printProfile(JSONObject profile) {
        System.out.println(profile.toString(2));
    }

    /**
     * Generate a profile for the user, print and save as json file
     *
     * @param user the user to generate profile for
     */
    public static void profile(String user) {
        // print profile to stdout
        System.out.printf("----- %s CHAT CLIENT PROFILE ----- %n", user.toUpperCase());
        LocalDateTime startProfile = LocalDateTime.now();
        JSONObject profile = generateProfile(user);
        LocalDateTime endProfile = LocalDateTime.now();
        System.out.printf("The profiling cost %d ms %n", Duration.between(startProfile, endProfile).toMillis());
        printProfile(profile);

        // save profile to file
        try {
            Path filePath = Paths.get(user + "-profile.json");
            Files.writeString(filePath, profile.toString(2));
            System.out.println("Profile generated at " + Utils.toInfo(filePath.toString()));
        } catch (IOException e) {
            Utils.printlnError("Fail to generate profile for " + user);
        }
    }
}
